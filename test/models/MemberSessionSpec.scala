package models

import java.time.LocalDateTime

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json

class MemberSessionSpec extends WordSpec with MustMatchers {

  "MemberSession model" must {
    val id = "a1b2c3d4e5f6g7h8"
    val time = LocalDateTime.now

    "serialise into JSON" in {
      val memberSession = MemberSession(
        _id = id,
        lastUpdated = time
      )

      val expectedJson = Json.obj(
        "_id" -> id,
        "lastUpdated" -> time
      )
      Json.toJson(memberSession) mustEqual expectedJson
    }

    "deserialise from JSON" in {
      val json = Json.obj(
        "_id" -> id,
        "lastUpdated" -> time
      )

      val expectedUser = MemberSession(
        _id = id,
        lastUpdated = time
      )
      json.as[MemberSession] mustEqual expectedUser
    }
  }
}
