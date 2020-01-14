package models

import models.Card.pathBindable
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json

class CardSpec extends WordSpec with OptionValues with MustMatchers {

  val validCard = "a1b2c3d4e5f6g7h8"

  "Card model" must {

    "deserialise" in {
      val cardId = Card(_id = validCard)
      val expectedJson = Json.obj("_id" -> validCard)

      Json.toJson(cardId) mustEqual expectedJson
    }

    "serialise" in {
      val expectedCardId = Card(_id = validCard)
      val json = Json.obj("_id" -> validCard)

      json.as[Card] mustEqual expectedCardId
    }

    "return 'The Card ID is invalid' if card id does not match regex pattern" in {
      val invalidCard = "4yDUm7Nra7ALHuDr!"
      val result = "The Card ID is invalid"

      pathBindable.bind("", invalidCard) mustBe Left(result)
    }

    "return a string" in {
      pathBindable.unbind("", Card("a1b2c3d4e5f6g7h8")) mustEqual "a1b2c3d4e5f6g7h8"
    }
  }
}
