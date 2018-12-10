package tinkoff.fintech.service.quest

import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.JsonCodec
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import tinkoff.fintech.service.data

import tinkoff.fintech.service.data.Client


sealed trait Request

@JsonCodec
final case class AddProducts(id: Int, products: Seq[data.Product]) extends Request

@JsonCodec
final case class CreateCheck(products: Seq[data.Product], idPaidClient: Int) extends Request


final case class CreateClient(client: Client) extends Request

object CreateClient{
  implicit val decoder: Decoder[CreateClient] = deriveDecoder[Client] map CreateClient.apply
  implicit val encoder: Encoder[CreateClient] = deriveEncoder[Client] contramap (_.client)
}

@JsonCodec
final case class Connect(checkId: Int, clientId: Int, productId: Int) extends Request

@JsonCodec
final case class GetCheck(id: Int) extends Request

@JsonCodec
final case class GetSumRerMonth(id: Int) extends Request

