package tinkoff.fintech.service.quest


import io.circe.Decoder.Result
import io.circe.generic.JsonCodec
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}


sealed trait Response

case object Ok extends Response

case object Fail extends Response

@JsonCodec
case class OkCreate(id: Int) extends Response


object Response {

  implicit val encoder: Encoder[Response] = {
    case Ok => ("status" -> "ok").asJson
    case Fail => ("status" -> "fail").asJson
    case OkCreate(id) => Json.obj("status" -> Json.fromString("ok-create"), "id" -> Json.fromInt(id))
  }

}
