package models

case class Member(
                   card: Card,
                   name: String,
                   email: String,
                   mobileNumber: String,
                   funds: Int,
                   pin: Int
                 )
