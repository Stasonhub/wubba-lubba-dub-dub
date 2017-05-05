package model

case class User(id: Int,
                phone: Long,
                name: String,
                trustRate: Long,
                password: Option[String],
                registered: Boolean)
