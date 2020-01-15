package controllers.actions

import java.time.LocalDateTime

import akka.stream.Materializer
import models.{Card, MemberSession}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService

import scala.concurrent.Future

class UpdateSessionActionSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  val mockSessionService: SessionService = mock[SessionService]

  private lazy val app: Application =
    new GuiceApplicationBuilder().overrides(
      bind[SessionService].toInstance(mockSessionService),
    ).build()

  implicit lazy val mat: Materializer = app.materializer

  private val updateSessionActionProvider = app.injector.instanceOf[UpdateSessionActionProvider]

  private val card = Card("a1b2c3d4e5f6g7h8")
  private val now = LocalDateTime.now

  override def beforeEach() = {
    reset(mockSessionService)
  }

  "UpdateSessionActionProvider" must {
    "allow the request through and update the session when a session exists" in {
      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(true))
      when(mockSessionService.updateSession(any)).thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      val action: Action[AnyContent] = updateSessionActionProvider(card) { request => Ok("") }

      val result = call(action, FakeRequest())

      status(result) mustEqual OK
      verify(mockSessionService, times(1)).updateSession(card)
    }

    "allow the request through and do not update the session when a session does not exist" in {
      when(mockSessionService.isUserLoggedIn(any)).thenReturn(Future.successful(false))

      val action: Action[AnyContent] = updateSessionActionProvider(card) { request => Ok("") }

      val result = call(action, FakeRequest())

      status(result) mustEqual OK
      verify(mockSessionService, times(0)).updateSession(card)
    }
  }
}
