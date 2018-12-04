package tinkoff.fintech.service.data

import java.sql.Timestamp
import java.time.LocalDateTime

import io.circe.generic.JsonCodec

@JsonCodec
final case class Check(id: Option[Int],
                       products: Seq[Product],
                       paidClient: Client,
                       time: Option[LocalDateTime] = Some(LocalDateTime.now())) {

  def +(product: Product): Check =
    this ++ Seq(product)

  def -(product: Product): Check =
    copy(products = products.filter(p => p != product))

  def ++(ps: Seq[Product]): Check =
    copy(products = products ++ ps)

  def noPaidProducts: Seq[Product] =
    products.filter(!_.client.contains(paidClient))

  def noPaidClients: Map[Client, List[Product]] =
    products
      .filter(_.client.nonEmpty)
      .map(product => (product.client, product))
      .groupBy(_._1.get).mapValues(_.map(_._2).toList)

  def connect(client: Client, productId: Int): Check = {
    val (a, b) = products.partition(_.id.contains(productId))
    if (a.isEmpty)
      this
    else
      this.copy(products = a.map(_.copy(client = Some(client))) ++ b)
  }

  def full: Boolean =
    !products.exists(_.client.isEmpty)

}

final case class CheckBase(id: Int,
                           time: Timestamp,
                           clientId: Int)

object Check {
  def apply(products: Seq[Product], paidClient: Client): Check =
    new Check(None, products, paidClient, Some(LocalDateTime.now()))
}
