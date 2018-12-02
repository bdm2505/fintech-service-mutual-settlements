package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
case class Product(id: Option[Int], name: String, cost: Double)

final case class ProductBase(id: Option[Int],
                             name: String,
                             cost: Double,
                             checkId: Int,
                             clientId: Option[Int])
