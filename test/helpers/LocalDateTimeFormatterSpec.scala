package helpers

import java.time.{LocalDate, LocalDateTime}

import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json

class LocalDateTimeFormatterSpec extends WordSpec with MustMatchers with OptionValues with LocalDateTimeFormatter {

  "LocalDateTime" must {
    val date = LocalDate.of(2020, 1, 1).atStartOfDay

    val dateMillis = 1577836800000L

    val json = Json.obj(
      "$date" -> dateMillis
    )

    "serialise to Json" in {
      val result = Json.toJson(date)
      result mustEqual Json.obj(
        "$date" -> (dateMillis)
      )
    }

    "deserialise from Json" in {
      val result = json.as[LocalDateTime]
      result mustEqual date
    }

    "serialise/deserialise to the same value" in {
      val result = Json.toJson(date).as[LocalDateTime]
      result mustEqual date
    }
  }
}
