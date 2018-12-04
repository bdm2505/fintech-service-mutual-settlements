package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
final case class Product(id: Option[Int],
                         name: String,
                         cost: Double,
                         client: Option[Client]) {

  def connect(client: Client): Product =
    copy(client = Some(client))
}

final case class ProductBase(id: Option[Int],
                             name: String,
                             cost: Double,
                             checkId: Int,
                             clientId: Option[Int])

object Product {
  def apply(name: String, cost: Double): Product =
    new Product(None, name, cost, None)
}
