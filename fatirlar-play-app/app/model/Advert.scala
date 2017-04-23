package model

case class Advert(
                   id: Long,
                   publicationDate: Long,
                   district: District,
                   address: String,
                   floor: Int,
                   maxFloor: Int,
                   rooms: Int,
                   sq: Int,
                   price: Long,
                   withPublicServices: Boolean,
                   withDeposit: Boolean,
                   description: String,
                   conditions: Long,
                   bedrooms: Int,
                   beds: Int,
                   latitude: Double,
                   longitude: Double
                 )