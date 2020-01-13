package controllers

import com.google.inject.{Inject, Singleton}
import models.Card
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class LoginController @Inject()(val controllerComponents: ControllerComponents)
                               (implicit ec: ExecutionContext) extends BaseController {

  def presentCard(card: Card, pin: Option[Int]): Action[AnyContent] = Action.async {
    implicit request =>
      ???
  }
}
