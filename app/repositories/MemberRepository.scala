package repositories

import com.google.inject.Inject
import models.{Card, Member}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.commands.{FindAndModifyCommand, WriteResult}
import reactivemongo.play.json.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.{ExecutionContext, Future}

class MemberRepository @Inject()(cc: ControllerComponents, config: Configuration, mongo: ReactiveMongoApi)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val collection: Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection]("members"))

  private def findAndUpdate(collection: JSONCollection, selection: JsObject,
                            modifier: JsObject): Future[FindAndModifyCommand.Result[collection.pack.type]] = {
    collection.findAndUpdate(
      selector = selection,
      update = modifier,
      fetchNewObject = true,
      upsert = false,
      sort = None,
      fields = None,
      bypassDocumentValidation = false,
      writeConcern = WriteConcern.Default,
      maxTime = None,
      collation = None,
      arrayFilters = Seq.empty
    )
  }

  def registerMember(newMember: Member): Future[WriteResult] = {
    collection.flatMap(_.insert.one(newMember))
  }

  def findMemberById(card: Card): Future[Option[Member]] = {
    collection.flatMap(_.find(
    Json.obj("_id" -> card._id),
    None
    ).one[Member])
  }

  def addFundsById(card: Card, increase: Int): Future[Option[Member]] = {
    collection.flatMap {
      result =>
        val selector: JsObject = Json.obj("_id" -> card._id)
        val modifier: JsObject = Json.obj("$inc" -> Json.obj("funds" -> increase))
        findAndUpdate(result, selector, modifier).map(_.result[Member])
    }
  }

  def removeMemberById(card: Card): Future[Option[JsObject]] = {
    collection.flatMap(
      _.findAndRemove(Json.obj("_id" -> card._id), None, None, WriteConcern.Default, None, None, Seq.empty)
        .map(_.value)
    )
  }

  def transactionById(card: Card, decrease: Int): Future[Option[Member]] = {
    collection.flatMap {
      result =>
        val selector: JsObject = Json.obj("_id" -> card._id)
        val modifier: JsObject = Json.obj("$inc" -> Json.obj("funds" -> -decrease))
        findAndUpdate(result, selector, modifier).map(_.result[Member])
    }
  }

  def updateNameById(card: Card, newName: String): Future[Option[Member]] = {
    collection.flatMap {
      result =>
        val selector: JsObject = Json.obj("_id" -> card._id)
        val modifier: JsObject = Json.obj("$set" -> Json.obj("name" -> newName))
        findAndUpdate(result, selector, modifier).map(_.result[Member])
    }
  }

  def updateMobileNumberById(card: Card, newNumber: String): Future[Option[Member]] = {
    collection.flatMap {
      result =>
        val selector: JsObject = Json.obj("_id" -> card._id)
        val modifier: JsObject = Json.obj("$set" -> Json.obj("mobileNumber" -> newNumber))
        findAndUpdate(result, selector, modifier).map(_.result[Member])
    }
  }
}
