package tinkoff.fintech.service.quest


import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import tinkoff.fintech.service.data.Check


sealed trait Response

case class Fail(msg: String) extends Response

case class OkCreate(id: Int) extends Response

case class OkCheck(check: Check) extends Response


object Response {


  def obj[T : Encoder](status: String, name: String, t: T): Json =
    Json.obj("status" -> status.asJson, name -> t.asJson)

  implicit val encoder: Encoder[Response] = {

    case Fail(msg) => obj("fail", "message", msg)
    case OkCreate(id) => obj("ok-create", "id", id)
    case OkCheck(check) => obj("ok-check", "check", check)
  }
  implicit val decoder: Decoder[Response] = (c: HCursor) => {
    for {
      status <- c.downField("status").as[String]
      e <- status match {

        case "fail" => c.downField("message").as[String].map(Fail)
        case "ok-create" => c.downField("id").as[Int].map(OkCreate)
        case "ok-check" => c.downField("check").as[Check].map(OkCheck)
      }
    } yield e
  }

}
