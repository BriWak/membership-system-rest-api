package services

import java.time.LocalDateTime

import models.{Card, MemberSession}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.SessionRepository

import scala.concurrent.Future

class SessionServiceSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  private lazy val app: Application =
    new GuiceApplicationBuilder().overrides(
      bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()

  private val sessionService = app.injector.instanceOf[SessionService]

  private val card = Card("a1b2c3d4e5f6g7h8")

  private val now = LocalDateTime.now

  "isUserLoggedIn" must {
    "return true if a session exists for the given card id" in {
      when(mockSessionRepository.findSessionById(any()))
        .thenReturn(Future.successful(Some(MemberSession("a1b2c3d4e5f6g7h8", LocalDateTime.now))))

      whenReady(sessionService.isUserLoggedIn(card)) { result =>
        result mustEqual true
      }
    }

    "return false if a session does not exist for the given card id" in {
      when(mockSessionRepository.findSessionById(any()))
        .thenReturn(Future.successful(None))

      whenReady(sessionService.isUserLoggedIn(card)) { result =>
        result mustEqual false
      }
    }
  }

  "updateSession" must {
    "update the session with the current time" in {
      when(mockSessionRepository.updateSessionById(any()))
        .thenReturn(Future.successful(Some(MemberSession(card._id, now))))

      whenReady(sessionService.updateSession(card)) { result =>
        result mustBe Some(MemberSession(card._id, now))
      }
    }
  }
}
