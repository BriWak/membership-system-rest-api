package controllers

import models.{Card, Member}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.MemberRepository

import scala.concurrent.Future

class UpdateControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  val mockMemberRepository: MemberRepository = mock[MemberRepository]

  private lazy val app: Application =
    new GuiceApplicationBuilder().overrides(
      bind[MemberRepository].toInstance(mockMemberRepository),
    ).build()

  private val card = Card("a1b2c3d4e5f6g7h8")
  private val pin = 1234
  private val name = "testName"
  private val email = "testEmail"
  private val mobile = "testMobile"

  "updateName" must {
    "return an OK response with success message if the member is found" in {
      when(mockMemberRepository.updateNameById(any, any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 200, pin))))

      when(mockMemberRepository.findMemberById(any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 200, pin))))

      val request = FakeRequest(POST, routes.UpdateController.updateName(Card("a1b2c3d4e5f6g7h8"), "John Smith").url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "The name stored for the id a1b2c3d4e5f6g7h8 has been updated to John Smith."
    }

    "return a NOT_FOUND response with correct error message when the member could not be found" in {
      when(mockMemberRepository.updateNameById(any, any))
        .thenReturn(Future.successful(None))

      val request =
        FakeRequest(POST, routes.UpdateController.updateName(Card("incorrectId12345"), "John Smith").url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "A member could not be found with card id: incorrectId12345."
    }

    "return a BAD_REQUEST response with correct error message when something else fails" in {
      when(mockMemberRepository.updateNameById(any, any))
        .thenReturn(Future.failed(new Exception))

      val request =
        FakeRequest(POST, routes.UpdateController.updateName(Card("incorrectId12345"), "John Smith").url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."
    }
  }

  "updateMobileNumber" must {
    "return an OK response with success message when data is valid" in {
      when(mockMemberRepository.updateMobileNumberById(any, any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 200, pin))))

      when(mockMemberRepository.findMemberById(any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 200, pin))))

      val request = FakeRequest(POST, routes.UpdateController.updateMobileNumber(Card("a1b2c3d4e5f6g7h8"), "07123456789").url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "The mobile number stored for the id a1b2c3d4e5f6g7h8 has been updated to 07123456789."
    }

    "return a NOT_FOUND response with correct error message when member could not be found" in {
      when(mockMemberRepository.updateMobileNumberById(any, any))
        .thenReturn(Future.successful(None))

      val request =
        FakeRequest(POST, routes.UpdateController.updateMobileNumber(Card("incorrectId12345"), "07123456789").url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "A member could not be found with card id: incorrectId12345."
    }

    "return a BAD_REQUEST response with correct error message when something else fails" in {
      when(mockMemberRepository.updateMobileNumberById(any, any))
        .thenReturn(Future.failed(new Exception))

      val request =
        FakeRequest(POST, routes.UpdateController.updateMobileNumber(Card("incorrectId12345"), "07123456789").url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."
    }
  }
}
