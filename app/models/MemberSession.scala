package models

import java.time.LocalDateTime

import helpers.LocalDateTimeFormatter
import play.api.libs.json._

case class MemberSession(_id: String, lastUpdated: LocalDateTime)

object MemberSession extends LocalDateTimeFormatter{
  implicit lazy val format: OFormat[MemberSession] = Json.format[MemberSession]
}
