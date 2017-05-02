package model

case class Advert(
                   id: Int,
                   publicationDate: Long,
                   district: District,
                   address: String,
                   floor: Int,
                   maxFloor: Int,
                   rooms: Int,
                   sq: Int,
                   price: Int,
                   withPublicServices: Boolean,
                   conditions: Int,
                   description: String,
                   latitude: Double,
                   longitude: Double,
                   beds: Int,
                   bedrooms: Int
                 )