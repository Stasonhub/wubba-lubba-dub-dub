package service.provider.api

case class RawAdvert(
                      publicationDate: Long,
                      bedrooms: Int,
                      beds: Int,
                      rooms: Int,
                      sq: Int,
                      floor: Int,
                      maxFloor: Int,
                      address: String,
                      description: String,
                      latitude: Double,
                      longitude: Double,
                      price: Int,
                      photos: List[String],
                      userName: String,
                      phone: Long,
                      trustRate: Int,
                      originId: Int
               )
