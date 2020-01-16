package controllers

import models.{Card, Member}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.core.errors.DatabaseException
import repositories.MemberRepository

import scala.concurrent.Future

class MembershipControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  val mockMemberRepository: MemberRepository = mock[MemberRepository]

  private lazy val app: Application =
    new GuiceApplicationBuilder().overrides(
      bind[MemberRepository].toInstance(mockMemberRepository)
    ).build()

  private val card = Card("a1b2c3d4e5f6g7h8")
  private val pin = 1234
  private val name = "testName"
  private val email = "testEmail"
  private val mobile = "testMobile"

  "findMemberById" must {
    "return an OK response with the member details if the member is found" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.successful(Some(Member(card, name, email, mobile, 200, pin))))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MembershipController.findMemberById(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) must contain
      """{"_id":card,"name":testName,"email":"testEmail","mobileNumber":"testMobile","funds":200,"pin":1234}""".stripMargin
    }

    "return a NOT_FOUND response with correct message when the member could not be found" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.successful(None))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MembershipController.findMemberById(Card("incorrectId12345")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "A member could not be found with card id: incorrectId12345."
    }

    "return a BAD_REQUEST response if data is invalid" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MembershipController.findMemberById(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Member model."
    }

    "return a BAD_REQUEST response if something else has failed" in {
      when(mockMemberRepository.findMemberById(any()))
        .thenReturn(Future.failed(new Exception))

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.MembershipController.findMemberById(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."
    }
  }

  "registerMember" must {
    "return an OK response with success message if data is valid" in {
      when(mockMemberRepository.registerMember(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val memberJson: JsValue = Json.toJson(Member(card, name, email, mobile, 200, pin))

      val request: FakeRequest[JsValue] =
        FakeRequest(POST, routes.MembershipController.registerMember().url).withBody(memberJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Member testName has been registered successfully with id a1b2c3d4e5f6g7h8."
    }

    "return a BAD_REQUEST response with correct error message when the data is invalid" in {
      val memberJson: JsValue = Json.toJson("Invalid Json")

      val request = FakeRequest(POST, routes.MembershipController.registerMember().url).withBody(memberJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Member model."
    }

    "return a BAD_REQUEST response with correct error message when duplicate data is given" in {
      when(mockMemberRepository.registerMember(any()))
        .thenReturn(Future.failed(new DatabaseException {
          override def originalDocument: Option[BSONDocument] = None

          override def code: Option[Int] = None

          override def message: String = "Duplicate key - unable to parse Json to the Member model."
        }))

      val memberJson: JsValue = Json.toJson(Member(card, name, email, mobile, 200, pin))

      val request =
        FakeRequest(POST, routes.MembershipController.registerMember().url).withBody(memberJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Duplicate key - unable to parse Json to the Member model."
    }

    "return a BAD_REQUEST response with correct error message when something else fails" in {
      when(mockMemberRepository.registerMember(any()))
        .thenReturn(Future.failed(new Exception))

      val memberJson: JsValue = Json.toJson(Member(card, name, email, mobile, 200, pin))

      val request =
        FakeRequest(POST, routes.MembershipController.registerMember().url).withBody(memberJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."
    }
  }

  "removeMember" must {
    "return an OK response with success message if data is valid" in {
      when(mockMemberRepository.removeMemberById(any()))
        .thenReturn(Future.successful(Some(Json.obj(
          "_id" -> card,
          "name" -> name,
          "email" -> email,
          "mobileNumber" -> mobile,
          "funds" -> 200,
          "pin" -> 1234
        ))))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, routes.MembershipController.removeMember(Card("a1b2c3d4e5f6g7h8")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Member removed successfully."
    }

    "return a NOT_FOUND response with correct error message when the member could not be found" in {
      when(mockMemberRepository.removeMemberById(any()))
        .thenReturn(Future.successful(None
        ))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, routes.MembershipController.removeMember(Card("a1b2c3d4e5f6g7h8")).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "A member could not be found with card id: a1b2c3d4e5f6g7h8."
    }

    "return a BAD_REQUEST response if something else has failed" in {
      when(mockMemberRepository.removeMemberById(any()))
        .thenReturn(Future.failed(new Exception))

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, routes.MembershipController.removeMember(Card("a1b2c3d4e5f6g7h8")).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."
    }
  }
}
