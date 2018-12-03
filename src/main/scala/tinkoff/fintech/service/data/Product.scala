package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
case class Product(id: Option[Int],
                   name: String,
                   cost: Double,
                   client: Option[Client])

final case class ProductBase(id: Option[Int],
                             name: String,
                             cost: Double,
                             checkId: Int,
                             clientId: Option[Int])

object Product {
  def apply(name: String, cost: Double): Product =
    new Product(None, name, cost, None)
}
