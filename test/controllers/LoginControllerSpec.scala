package controllers

import java.time.LocalDateTime

import models.{Card, Member, MemberSession}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsResultException
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import repositories.{MemberRepository, SessionRepository}
import services.SessionService

import scala.concurrent.Future

class LoginControllerSpec extends PlaySpec with MustMatchers with MockitoSugar with ScalaFutures {

  val mockMemberRepository: MemberRepository = mock[MemberRepository]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockSessionService: SessionService = mock[SessionService]

  private lazy val app: Application =
    new GuiceApplicationBuilder().overrides(
      bind[MemberRepository].toInstance(mockMemberRepository),
      bind[SessionRepository].toInstance(mockSessionRepository),
      bind[SessionService].toInstance(mockSessionService),
    ).build()

  private val card = Card("a1b2c3d4e5f6g7h8")
  private val pin = 1234
  private val name = "testName"
  private val email = "testEmail"
  private val mobile = "testMobile"
  private val now =  LocalDateTime.now

  "presentCard" must {
    "return an OK response and create a new session if a session does not exist" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 200, pin))))

      when(mockSessionRepository.findSessionById(any()))
        .thenReturn(Future.successful(None))

      when(mockSessionRepository.createSession(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
        GET, routes.LoginController.presentCard(Card("a1b2c3d4e5f6g7h8"), Some(pin)).url
      )

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Welcome testName."
    }

    "return an OK response and delete current session if a session already exists" in {
      when(mockSessionService.updateSession(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 200, pin))))

      when(mockSessionRepository.findSessionById(any()))
        .thenReturn(Future.successful(Some(MemberSession("a1b2c3d4e5f6g7h8", LocalDateTime.now))))

      when(mockSessionRepository.deleteSessionById(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
        GET, routes.LoginController.presentCard(Card("a1b2c3d4e5f6g7h8"), None).url
      )

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Goodbye testName."
    }

    "return a BAD_REQUEST response and the correct message if the member does not exist" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.successful(None))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
        GET, routes.LoginController.presentCard(Card("a1b2c3d4e5f6g7h8"), Some(pin)).url
      )

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Your card is not registered. Please register your card."
    }

    "return a BAD_REQUEST response if the data is invalid" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
        GET, routes.LoginController.presentCard(Card("a1b2c3d4e5f6g7h8"), Some(pin)).url
      )

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Member model."
    }

    "return a BAD_REQUEST response if something else has failed" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.failed(new Exception))

      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
        GET, routes.LoginController.presentCard(Card("a1b2c3d4e5f6g7h8"), Some(pin)).url
      )

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An error has occurred resulting in the following exception: java.lang.Exception."
    }
  }
}
