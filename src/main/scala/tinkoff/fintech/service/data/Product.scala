package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
case class Product(name: String, cost: Double)
