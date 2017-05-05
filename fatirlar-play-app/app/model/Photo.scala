package model

case class Photo(id: Int,
                 advertId: Int,
                 path: String,
                 main: Boolean,
                 hash: Long)
