package tinkoff.fintech.service.quest


import java.time.LocalDateTime

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import tinkoff.fintech.service.data.{Check, Client}


sealed trait Response

case class Fail(msg: String) extends Response

case class OkClient(client: Client) extends Response

case class OkCheck(check: Check) extends Response

case class OkSumPerMouth(values: Map[String, Double]) extends Response


object Response {


  def obj[T : Encoder](status: String, name: String, t: T): Json =
    Json.obj("status" -> status.asJson, name -> t.asJson)

  implicit val encoder: Encoder[Response] = {

    case Fail(msg) => obj("fail", "message", msg)
    case OkClient(client) => obj("ok-client", "client", client)
    case OkCheck(check) => obj("ok-check", "check", check)
    case OkSumPerMouth(v) => obj("ok-sum-per-mouth", "values", v)
  }
  implicit val decoder: Decoder[Response] = (c: HCursor) => {
    for {
      status <- c.downField("status").as[String]
      e <- status match {
        case "fail" => c.downField("message").as[String].map(Fail)
        case "ok-client" => c.downField("client").as[Client].map(OkClient)
        case "ok-check" => c.downField("check").as[Check].map(OkCheck)
        case "ok-sum-per-mouth" => c.downField("values").as[Map[String, Double]].map(OkSumPerMouth)
      }
    } yield e
  }

}
