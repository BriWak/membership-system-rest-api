package controllers

import com.google.inject.{Inject, Singleton}
import controllers.actions.UpdateSessionActionProvider
import models.{Card, Member}
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc._
import reactivemongo.core.errors.DatabaseException
import repositories.{MemberRepository, SessionRepository}
import services.SessionService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class MembershipController @Inject()(cc: ControllerComponents,
                                     memberRepository: MemberRepository,
                                     sessionRepository: SessionRepository,
                                     sessionService: SessionService,
                                     updateSessionAction: UpdateSessionActionProvider)
                                    (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def findMemberById(card: Card): Action[AnyContent] = Action.async {
    implicit request =>
      memberRepository.findMemberById(card).map {
        case None => NotFound(s"A member could not be found with card id: ${card._id}.")
        case Some(member) => Ok(Json.toJson(member))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Member model."))
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def registerMember: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      (for {
        member <- Future.fromTry(Try {
          request.body.as[Member]
        })
        _ <- memberRepository.registerMember(member)
      } yield Ok(s"Member ${member.name} has been registered successfully with id ${member.card._id}.")).recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Member model."))
        case _: DatabaseException =>
          Future.successful(BadRequest("Duplicate key - unable to parse Json to the Member model."))
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def removeMember(card: Card): Action[AnyContent] = Action.async {
    implicit request =>
      memberRepository.removeMemberById(card).map {
        case Some(_) => Ok("Member removed successfully.")
        case _ => NotFound(s"A member could not be found with card id: ${card._id}.")
      } recoverWith {
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }
}
