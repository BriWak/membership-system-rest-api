package controllers

import com.google.inject.{Inject, Singleton}
import controllers.actions.UpdateSessionActionProvider
import models.Card
import play.api.mvc._
import repositories.MemberRepository
import services.SessionService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateController @Inject()(cc: ControllerComponents,
                                 memberRepository: MemberRepository,
                                 sessionService: SessionService,
                                 updateSessionAction: UpdateSessionActionProvider)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def updateName(card: Card, newName: String): Action[AnyContent] = updateSessionAction(card).async {
    implicit request =>
      memberRepository.updateNameById(card, newName).map {
        case Some(member) =>
          Ok(s"The name stored for the id ${member.card._id} has been updated to $newName.")
        case _ =>
          NotFound(s"A member could not be found with card id: ${card._id}.")
      } recoverWith {
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def updateMobileNumber(card: Card, newNumber: String): Action[AnyContent] = updateSessionAction(card).async {
    implicit request =>
      memberRepository.updateMobileNumberById(card, newNumber).map {
        case Some(member) =>
          Ok(s"The mobile number stored for the id ${member.card._id} has been updated to $newNumber.")
        case _ =>
          NotFound(s"A member could not be found with card id: ${card._id}.")
      } recoverWith {
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }
}
