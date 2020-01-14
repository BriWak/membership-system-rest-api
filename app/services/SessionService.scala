package services

import com.google.inject.Inject
import models.{Card, MemberSession}
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class SessionService @Inject()(sessionRepository: SessionRepository)(implicit ec: ExecutionContext) {

  def isUserLoggedIn(card: Card): Future[Boolean] = {
    sessionRepository.findSessionById(card).map(_.isDefined)
  }

  def updateSession(card: Card): Future[Option[MemberSession]] = {
    sessionRepository.updateSessionById(card)
  }
}
