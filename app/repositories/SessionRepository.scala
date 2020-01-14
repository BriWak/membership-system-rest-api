package repositories

import java.time.LocalDateTime

import com.google.inject.Inject
import models.{Card, MemberSession}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.commands.{FindAndModifyCommand, WriteResult}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

class SessionRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration,
                                  memberRepository: MemberRepository)(implicit ec: ExecutionContext) {

  private val collection: Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection]("session"))

  val timeToLive: Int = config.get[Int]("session.timeToLive")

  private val index: Index = Index(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = Some("session-index"),
    options = BSONDocument("expireAfterSeconds" -> timeToLive)
  )

  collection.map(_.indexesManager.ensure(index))

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

  def createSession(session: MemberSession): Future[WriteResult] =
    collection.flatMap(_.insert.one(session))

  def findSessionById(card: Card): Future[Option[MemberSession]] =
    collection.flatMap(_.find(Json.obj("_id" -> card._id), None).one[MemberSession])

  def updateSessionById(card: Card): Future[Option[MemberSession]] = {
    collection.flatMap {
      result =>
        val selector: JsObject = Json.obj("_id" -> card._id)
        val modifier = Json.obj("$set" -> MemberSession(card._id, LocalDateTime.now))
        findAndUpdate(result, selector, modifier).map(_.result[MemberSession])
    }
  }

  def deleteSessionById(card: Card): Future[WriteResult] =
    collection.flatMap(_.delete.one(Json.obj("_id" -> card._id)))
}
