package service.provider.api

case class VerifyAdvert(publicationDate: Long,
                        rooms: Int,
                        sq: Int,
                        floor: Int,
                        maxFloor: Int,
                        address: String,
                        latitude: Double,
                        longitude: Double,
                        price: Int,
                        phone: Long,
                        trustRate: Int,
                        originId: Int)
