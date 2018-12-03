package tinkoff.fintech.service.data

import io.circe.generic.JsonCodec

@JsonCodec
final case class Product(id: Option[Int], name: String, cost: Double, clientId: Option[Int]) {
  def connect(c: Int): Product =
    copy(clientId = Some(c))

}
object Product {
  def apply(name: String, cost: Double, clientId: Option[Int] = None, id: Option[Int] = None): Product =
    new Product(id, name, cost, clientId)

  def apply(id: Option[Int], name: String, cost: Double, clientId: Option[Int]): Product =
    new Product(id, name, cost, clientId)
}

//final case class ProductBase(id: Option[Int],
//                             name: String,
//                             cost: Double,
//                             checkId: Int,
//                             clientId: Option[Int],
//                            )
