package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
case class Client(id: Option[Int],
                  name: String,
                  email: String,
                  phone: Option[String],
                  cardNumber: Option[String])
