package model

case class User(id: Long,
                phone: Long,
                name: String,
                password: String,
                trustRate: Int,
                registered: Boolean)
