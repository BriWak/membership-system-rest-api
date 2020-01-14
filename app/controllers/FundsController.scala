package controllers

import com.google.inject.{Inject, Singleton}
import controllers.actions.UpdateSessionActionProvider
import models.Card
import play.api.libs.json.{JsResultException, Json}
import play.api.mvc._
import repositories.MemberRepository
import services.SessionService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FundsController @Inject()(cc: ControllerComponents,
                                memberRepository: MemberRepository,
                                sessionService: SessionService,
                                updateSessionAction: UpdateSessionActionProvider)
                               (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def addFunds(card: Card, increase: Int): Action[AnyContent] = updateSessionAction(card).async {
    implicit request =>
      sessionService.isUserLoggedIn(card).flatMap {
        case false => Future.successful(BadRequest("Please log in to add funds."))
        case true =>
          memberRepository.findMemberById(card).flatMap {
            case Some(_) =>
              increase match {
                case amount if amount <= 0 => Future.successful(
                  BadRequest("You must provide a positive amount to increase your funds.")
                )
                case _ => memberRepository.addFundsById(card, increase).map { _ =>
                  Ok(s"Additional funds of $increase have been added to your card.")
                }
              }
            case None => Future.successful(NotFound(s"A member could not be found with card id: ${card._id}."))
          }
      } recoverWith {
        case _: JsResultException => Future.successful(
          BadRequest("Incorrect data - unable to parse Json data to the Member model.")
        )
        case e => Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def checkFunds(card: Card): Action[AnyContent] = updateSessionAction(card).async {
    implicit request =>
      sessionService.isUserLoggedIn(card).flatMap {
        case false => Future.successful(BadRequest("Please log in to check your funds."))
        case true =>
          memberRepository.findMemberById(card).map {
            case Some(member) => Ok(Json.toJson(member.funds))
            case None => NotFound(s"A member could not be found with card id: ${card._id}.")
          } recoverWith {
            case _: JsResultException =>
              Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Member model."))
            case e =>
              Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
          }
      }
  }

  def purchaseGoods(card: Card, amount: Int): Action[AnyContent] = updateSessionAction(card).async {
    implicit request =>
      sessionService.isUserLoggedIn(card).flatMap {
        case false => Future.successful(BadRequest("Please log in to purchase goods."))
        case true =>
          memberRepository.findMemberById(card).flatMap {
            case Some(member) => {
              if (amount > member.funds)
                Future.successful(BadRequest("You do not have enough funds to complete this transaction."))
              else memberRepository.transactionById(card, amount).map { _ =>
                Ok("Your transaction was successful.")
              }
            }
            case None => Future.successful(NotFound(s"A member could not be found with card id: ${card._id}."))
          }
      }.recoverWith {
        case _: JsResultException => Future.successful(
          BadRequest("Incorrect data - unable to parse Json data to the Member model.")
        )
        case e => Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }
}
