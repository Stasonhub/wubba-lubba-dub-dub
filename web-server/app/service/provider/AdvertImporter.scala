package service.provider

import javax.inject.Inject

import com.typesafe.scalalogging.Logger
import doobie.imports._
import doobie.postgres.pgtypes._
import fs2.interop.cats._
import model.{Advert, District, Photo, User}
import repository._
import service.provider.api.{RawAdvert, VerifyAdvert}
import service.provider.avito.AvitoParser
import service.provider.totook.TotookParser
import service.PhotoService
import service.location.LocationServiceJv

import scala.concurrent.duration._

class AdvertImporter @Inject()(
                                dbConnection: DbConnection,
                                avitoParser: AvitoParser,
                                totookParser: TotookParser,
                                advertImportRepository: AdvertImportRepository,
                                advertRepository: AdvertRepository,
                                userRepository: UserRepository,
                                photoRepository: PhotoRepository,
                                photoPersistService: PhotoPersistService,
                                photoService: PhotoService,
                                locationService: LocationServiceJv
                              ) {

  implicit val districtAtom = pgJavaEnum[District]("districts_enum")

  val logger = Logger[AdvertImporter]
  val avtType = "AVT"
  val ttkType = "TTK"

  def runImport(): Unit = {
    val lastImportTimeAvt = advertImportRepository.lastImportTime(avtType)
      .unique
      .transact(dbConnection.xa)
      .unsafePerformIO
    runImportAvito(lastImportTimeAvt, System.currentTimeMillis())

    val lastImportTimeTtk = advertImportRepository.lastImportTime(ttkType)
      .unique
      .transact(dbConnection.xa)
      .unsafePerformIO
    runVerifyTotook(lastImportTimeTtk, System.currentTimeMillis() - 10.minutes.toMillis)
  }

  def runVerifyTotook(fromTimestamp: Long, toTimestamp: Long) = {
    // run advert import from [ts to ts)
    // batched by 3 hours
    val batchSize = 3.hours.toMillis
    Stream.range(fromTimestamp, toTimestamp, batchSize)
      .map(startPeriod => {
        totookParser.parseAdverts(startPeriod, startPeriod + batchSize)
          .foreach(verifyAdvert)
        startPeriod + batchSize
      })
      .foreach(batchEndTs => saveLastImport(ttkType, batchEndTs))

    saveLastImport(ttkType, toTimestamp)
  }

  def runImportAvito(fromTimestamp: Long, toTimestamp: Long) = {
    // run advert import from [ts to ts)
    // batched by 3 hours
    val batchSize = 3.hours.toMillis
    Stream.range(fromTimestamp, toTimestamp, batchSize)
      .map(startPeriod => {
        avitoParser.parseAdverts(startPeriod, startPeriod + batchSize)
          .filter(checkAdvert)
          .foreach(persistAdvert)
        startPeriod + batchSize
      })
      .foreach(batchEndTs => saveLastImport(avtType, batchEndTs))

    saveLastImport(avtType, toTimestamp)
  }

  private def saveLastImport(importerType: String, timestamp: Long) = {
    logger.info(s"Saving import time $timestamp for $importerType")
    advertImportRepository
      .saveLastImportTime(importerType, timestamp)
      .run
      .transact(dbConnection.xa)
      .unsafePerformIO
  }

  private def checkAdvert(rawAdvert: RawAdvert): Boolean = {
    if (rawAdvert.address.isEmpty) {
      logger.warn(s"Address is empty for advert $rawAdvert")
      return false
    }
    if (rawAdvert.photos.isEmpty) {
      logger.warn(s"Photos is empty for advert $rawAdvert")
      return false
    }
    true
  }

  private def findMatchingAdvertByPhotos(rawAdvert: RawAdvert, photos: List[Photo]): Option[Advert] = {
    val advertIds = photoService.matchingAdverts(photos)

    if (advertIds.nonEmpty) {
      logger.warn(s"For incoming advert $rawAdvert found duplicates $advertIds")
      return advertRepository.findById(advertIds.head)
        .option
        .transact(dbConnection.xa)
        .unsafePerformIO
    }
    Option.empty
  }

  private def persistAdvert(rawAdvert: RawAdvert): Boolean = {
    val photos = photoPersistService.savePhotos(avtType, rawAdvert)
    val matchingAdvert = findMatchingAdvertByPhotos(rawAdvert, photos)
    val matchingUser = userRepository.findByPhone(rawAdvert.phone)
      .option
      .transact(dbConnection.xa)
      .unsafePerformIO

    if (matchingAdvert.isDefined) {
      /* full duplicate */
      if (matchingUser.isDefined) return false
      // found new user for the same advert
      // remove half of trust
      val user = userRepository
        .createUser(User(0,
          rawAdvert.phone,
          rawAdvert.userName,
          rawAdvert.trustRate / 2,
          Option.empty,
          registered = false))
        .withUniqueGeneratedKeys[User]("id", "phone", "name", "trustrate", "password", "registered")
        .transact(dbConnection.xa)
        .unsafePerformIO

      advertRepository
        .bindToUser(matchingAdvert.get.id, user.id)
        .run
        .transact(dbConnection.xa)
        .unsafePerformIO
      return false
    }

    val advert = advertRepository.createAdvert(
      Advert(0,
        rawAdvert.publicationDate,
        locationService.getDistrictFromAddress(rawAdvert.latitude, rawAdvert.longitude),
        rawAdvert.address,
        rawAdvert.floor,
        rawAdvert.maxFloor,
        rawAdvert.rooms,
        rawAdvert.sq,
        rawAdvert.price,
        withPublicServices = true,
        0,
        rawAdvert.description,
        rawAdvert.latitude,
        rawAdvert.longitude,
        rawAdvert.bedrooms,
        rawAdvert.beds,
        avtType,
        rawAdvert.originId))
      .withUniqueGeneratedKeys[Advert]("id", "publicationdate", "district", "address", "floor", "maxfloor", "rooms", "sq", "price", "withpublicservices", "conditions", "description", "latitude", "longitude", "beds", "bedrooms", "origintype", "originid")
      .transact(dbConnection.xa)
      .unsafePerformIO

    if (matchingUser.isDefined) { // found another one advert from the same user
      // remove 4x trust
      val user = User(matchingUser.get.id,
        matchingUser.get.phone,
        matchingUser.get.name,
        matchingUser.get.trustRate / 4,
        matchingUser.get.password,
        matchingUser.get.registered)

      userRepository
        .updateUser(user)
        .run
        .transact(dbConnection.xa)
        .unsafePerformIO
      advertRepository
        .bindToUser(advert.id, matchingUser.get.id)
        .run
        .transact(dbConnection.xa)
        .unsafePerformIO
    }
    else { // just create new user and bind advert
      val user = userRepository
        .createUser(User(0,
          rawAdvert.phone,
          rawAdvert.userName,
          rawAdvert.trustRate,
          Option.empty,
          registered = false))
        .withUniqueGeneratedKeys[User]("id", "phone", "name", "trustrate", "password", "registered")
        .transact(dbConnection.xa)
        .unsafePerformIO

      advertRepository
        .bindToUser(advert.id, user.id)
        .run
        .transact(dbConnection.xa)
        .unsafePerformIO
    }
    // persist photos
    for (photo <- photos) {
      photoRepository
        .createPhoto(Photo(0,
          advert.id,
          photo.path,
          photo.main,
          photo.hash))
        .withUniqueGeneratedKeys[Photo]("id", "advertid", "path", "main", "hash")
        .transact(dbConnection.xa)
        .unsafePerformIO
    }
    true
  }

  private def verifyAdvert(verifyAdvert: VerifyAdvert): Boolean = {
    val matchingAdverts = advertRepository
      .findBySqPriceCoords(verifyAdvert.sq, verifyAdvert.price, verifyAdvert.latitude, verifyAdvert.longitude)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
    if (matchingAdverts.isEmpty) {
      logger.warn(s"Verification. Failed to find advert by [sq/price/lat/lon]: [$verifyAdvert]")
      return false
    }
    if (matchingAdverts.size > 1) {
      logger.warn(s"Verification. Found more than one matching adverts for $verifyAdvert")
      return false
    }
    val matchingAdvert = matchingAdverts.head
    // find match by advert/partial user phone
    val matchingUsers = userRepository
      .findByStartingSixNumbers(matchingAdvert.id, verifyAdvert.phone.toInt)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
    if (matchingUsers.isEmpty) {
      logger.warn(s"Verification. Failed to find user for advert $verifyAdvert")
      return false
    }
    if (matchingUsers.size > 1) {
      logger.warn(s"Verification. Found more than one matching users for advert $verifyAdvert. Users ${matchingUsers.map(_.id)}")
      return false
    }
    // set current user new rate
    // set other users /4 rate
    userRepository.arrangeRate(matchingAdvert.id, matchingUsers.head.id, verifyAdvert.trustRate, 0.25)
      .run
      .transact(dbConnection.xa)
      .unsafePerformIO
    true
  }

}
