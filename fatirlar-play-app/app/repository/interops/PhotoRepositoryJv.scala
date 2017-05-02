package repository.interops

import java.util

import model.Photo

class PhotoRepositoryJv {
  def createPhoto(photo: Photo) = ???

  def deletePhoto(photo: Photo) = ???

  def getMainPhoto(advertId: Long): Photo = ???

  def getPhotos(advertIds: Long): util.List[Photo] = ???

  def getMainPhotos(advertIds: util.List[Long]): util.List[Photo] = ???

  def getAllPhotoHashes: util.List[Photo] = ???
}
