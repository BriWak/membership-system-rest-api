package controllers

import java.time.LocalDateTime

import models.{Card, Member, MemberSession}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsResultException
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.MemberRepository
import services.SessionService

import scala.concurrent.Future

class FundsControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  val mockMemberRepository: MemberRepository = mock[MemberRepository]
  val mockSessionService: SessionService = mock[SessionService]

  private lazy val app: Application =
    new GuiceApplicationBuilder().overrides(
      bind[MemberRepository].toInstance(mockMemberRepository),
      bind[SessionService].toInstance(mockSessionService)
    ).build()

  private val card = Card("a1b2c3d4e5f6g7h8")
  private val pin = 1234
  private val name = "testName"
  private val email = "testEmail"
  private val mobile = "testMobile"
  private val now =  LocalDateTime.now

  "addFunds" must {
    "return an OK response with success message if data is valid" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.addFundsById(any, any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockMemberRepository.findMemberById(any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.FundsController.addFunds(Card("a1b2c3d4e5f6g7h8"), 234).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Additional funds of 234 have been added to your card."
    }

    "return a NOT_FOUND response with correct error message when the member could not be found" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(Card("incorrectId12345")))
        .thenReturn(Future.successful(None))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request =
        FakeRequest(POST, routes.FundsController.addFunds(Card("incorrectId12345"), 500).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "A member could not be found with card id: incorrectId12345."
    }

    "return a BAD_REQUEST response with correct error message if the user is not logged in" in {
      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val request = FakeRequest(POST, routes.FundsController.addFunds(Card("a1b2c3d4e5f6g7h8"), 500).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Please log in to add funds."
    }

    "return a BAD_REQUEST response with correct error message if given a negative amount" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.FundsController.addFunds(Card("a1b2c3d4e5f6g7h8"), -200).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "You must provide a positive amount to increase your funds."
    }
  }

  "checkFunds" must {
    "return an OK response with correct funds when a correct card id is input" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.FundsController
        .checkFunds(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "500"
    }

    "return a NOT_FOUND response with correct message when member could not be found" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.successful(None))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.FundsController.checkFunds
      (Card("testId1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "A member could not be found with card id: testId1234567890."
    }

    "return a BAD_REQUEST response with correct error message if the user is not logged in" in {
      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val request = FakeRequest(GET, routes.FundsController.checkFunds(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Please log in to check your funds."
    }

    "return a BAD_REQUEST response if data is invalid" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.FundsController.checkFunds(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Member model."
    }

    "return a BAD_REQUEST response if something else has failed" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.failed(new Exception))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.FundsController.checkFunds(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."
    }
  }

  "purchaseGoods" must {
    "return an OK response with success message if data is valid" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.transactionById(any, any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockMemberRepository.findMemberById(any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.FundsController.purchaseGoods(Card("a1b2c3d4e5f6g7h8"), 500).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Your transaction was successful."
    }

    "return a NOT_FOUND response with correct error message when member could not be found" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockMemberRepository.findMemberById(Card("incorrectId12345")))
        .thenReturn(Future.successful(None))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request =
        FakeRequest(POST, routes.FundsController.purchaseGoods(Card("incorrectId12345"), 500).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "A member could not be found with card id: incorrectId12345."
    }

    "return a BAD_REQUEST response with correct error message if transaction cost is higher than total funds" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockMemberRepository.transactionById(any, any))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 500, pin))))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.FundsController.purchaseGoods(Card("a1b2c3d4e5f6g7h8"), 600).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "You do not have enough funds to complete this transaction."
    }

    "return a BAD_REQUEST response with correct error message if the user is not logged in" in {
      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val request = FakeRequest(POST, routes.FundsController.purchaseGoods(Card("a1b2c3d4e5f6g7h8"), 600).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Please log in to purchase goods."
    }
  }
}
