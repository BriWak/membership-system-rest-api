package models

import org.scalatest._
import play.api.libs.json.Json

class MemberSpec extends WordSpec with OptionValues with MustMatchers {

  val card: Card = Card("a1b2c3d4e5f6g7h8")

  "Member model" must {
    "deserialise correctly" in {
      val json = Json.obj(
        "_id" -> "a1b2c3d4e5f6g7h8",
        "name" -> "Test User",
        "email" -> "email@address.co.uk",
        "mobileNumber" -> "01234567890",
        "funds" -> 500,
        "pin" -> 1234
      )

      val expectedMember = Member(
        card = card,
        name = "Test User",
        email = "email@address.co.uk",
        mobileNumber = "01234567890",
        funds = 500,
        pin = 1234
      )
      json.as[Member] mustEqual expectedMember
    }

    "serialise correctly" in {
      val member = Member(
        card = card,
        name = "Test User",
        email = "email@address.co.uk",
        mobileNumber = "01234567890",
        funds = 500,
        pin = 1234
      )

      val expectedJson = Json.obj(
        "_id" -> "a1b2c3d4e5f6g7h8",
        "name" -> "Test User",
        "email" -> "email@address.co.uk",
        "mobileNumber" -> "01234567890",
        "funds" -> 500,
        "pin" -> 1234
      )
      Json.toJson(member) mustBe expectedJson
    }
  }
}
