package repository

import cats.implicits._
import doobie.imports.Fragments.{in, whereAndOpt}
import doobie.imports._
import model.Photo

class PhotoRepository {

  def createPhoto(photo: Photo): Update0 =
      sql"""
       INSERT INTO photo (advertId, path, main, hash)
       VALUES (${photo.advertId}, ${photo.path}, ${photo.main}, ${photo.hash})
       """.update
    // .withUniqueGeneratedKeys[Photo]("id", "advertId", "path", "main", "hash")

  def getPhotos(advertId: Int): Query0[Photo] =
      sql"""
           SELECT *
           FROM photo
           WHERE advertid = $advertId
            """.query[Photo]

  def getMainPhotos(advertIds: List[Int]): Query0[Photo] = {
    val advertIdIn = advertIds.toNel.map(cs => in(fr"advertid", cs))
    (fr"""
        SELECT *
        FROM photo""" ++
        whereAndOpt(Option(fr"main='true'"), advertIdIn)).query[Photo]
  }

  def getAllPhotoHashes: Query0[Photo] =
      sql"""
        SELECT *
        FROM photo""".query[Photo]
}
