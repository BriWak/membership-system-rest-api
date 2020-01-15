package helpers

import java.time._
import play.api.libs.json._

trait LocalDateTimeFormatter {

  implicit val localDateTimeRead: Reads[LocalDateTime] =
    (__ \ "$date").read[Long].map {
      millis => LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
    }

  implicit val localDateTimeWrite: Writes[LocalDateTime] = (dateTime: LocalDateTime) => Json.obj(
    "$date" -> dateTime.atOffset(ZoneOffset.UTC).toInstant.toEpochMilli
  )
}
