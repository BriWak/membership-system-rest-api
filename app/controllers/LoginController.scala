package controllers

import java.time.LocalDateTime

import com.google.inject.{Inject, Singleton}
import controllers.actions.UpdateSessionActionProvider
import models.{Card, MemberSession}
import play.api.libs.json.JsResultException
import play.api.mvc._
import repositories.{MemberRepository, SessionRepository}
import services.SessionService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginController @Inject()(val controllerComponents: ControllerComponents,
                                memberRepository: MemberRepository,
                                sessionRepository: SessionRepository,
                                sessionService: SessionService,
                                updateSessionAction: UpdateSessionActionProvider)
                               (implicit ec: ExecutionContext) extends BaseController {

  def presentCard(card: Card, pin: Option[Int]): Action[AnyContent] = updateSessionAction(card).async {
    implicit request =>
      memberRepository.findMemberById(card).flatMap {
        case Some(member) =>
          sessionService.isUserLoggedIn(card).flatMap {
            case true => sessionRepository.deleteSessionById(card).map(_ => Ok(s"Goodbye ${member.name}."))
            case false =>
              if (pin.isDefined && pin.get == member.pin)
                sessionRepository.createSession(MemberSession(card._id, LocalDateTime.now))
                  .map(_ => Ok(s"Welcome ${member.name}."))
              else if (pin.isEmpty) Future.successful(BadRequest("Please enter your pin to log in."))
              else Future.successful(BadRequest("The pin you have entered is incorrect, please try again."))
          }
        case None => Future.successful(BadRequest("Your card is not registered. Please register your card."))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Member model."))
        case e =>
          Future.successful(BadRequest(s"An error has occurred resulting in the following exception: $e."))
      }
  }
}
