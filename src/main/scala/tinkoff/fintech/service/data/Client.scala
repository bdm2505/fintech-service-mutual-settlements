package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
case class Client(id: Option[Int],
                  name: String,
                  email: String,
                  phone: Option[String],
                  cardNumber: Option[String])

object Client {

  def apply(name: String,
            email: String,
            phone: Option[String] = None,
            cardNumber: Option[String] = None,
            id: Option[Int] = None
           ): Client = new Client(id, name, email, phone, cardNumber)

  def apply(id: Option[Int],
            name: String,
            email: String,
            phone: Option[String],
            cardNumber: Option[String]): Client = new Client(id, name, email, phone, cardNumber)
}
