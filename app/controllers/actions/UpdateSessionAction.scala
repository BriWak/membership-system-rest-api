package controllers.actions

import com.google.inject.Inject
import models.Card
import play.api.mvc._
import services.SessionService

import scala.concurrent.{ExecutionContext, Future}

class UpdateSessionAction @Inject()(card: Card,
                                    sessionService: SessionService,
                                    val parser: BodyParsers.Default
                                   )(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[Request, AnyContent] with ActionFilter[Request] {

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
    sessionService.isUserLoggedIn(card).flatMap { userIsLoggedIn =>
      if (userIsLoggedIn) sessionService.updateSession(card).map(_ => None)
      else Future.successful(None)
    }
  }
}

class UpdateSessionActionProvider @Inject()(sessionService: SessionService,
                                            parser: BodyParsers.Default)(implicit ec: ExecutionContext) {
  def apply(card: Card) = new UpdateSessionAction(card, sessionService, parser)
}
