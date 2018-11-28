package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
case class Client(name: String, email: String, props: String)
