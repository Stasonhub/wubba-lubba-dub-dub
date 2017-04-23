package model

case class Photo(id: Long,
                 advertId: Long,
                 main: Boolean,
                 path: String,
                 hash: Long)
