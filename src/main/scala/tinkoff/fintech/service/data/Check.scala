package tinkoff.fintech.service.data

import java.sql.Timestamp
import java.time.LocalDateTime

final case class Check(id: Option[Int],
                       products: Set[Product],
                       paidClient: Client,
                       time: Option[LocalDateTime] = Some(LocalDateTime.now())) {

  def noPaidClients: Map[Client, List[Product]] =
    products
      .filter(_.client.nonEmpty)
      .map(product => (product.client, product))
      .groupBy(_._1.get).mapValues(_.map(_._2).toList)

  def connect(client: Client, name: String): Check = {
    val (a, b) = products.partition(_.name == name)
    if (a.isEmpty)
      this
    else
      this.copy(products = a.map(_.copy(client = Some(client))) ++ b)
  }

  def + (product: Product): Check =
    copy(products = products + product)

  def - (product: Product): Check =
    copy(products = products - product)

  def ++ (ps: Seq[Product]): Check =
    copy(products = products ++ ps)

  def filterNoPaid: Seq[Product] =
    products.filter(!_.clientId.contains(paidClientId)).toSeq

}

final case class CheckBase(id: Int,
                           time: Timestamp,
                           clientId: Int)

object Check {
  def apply(products: Seq[Product], paidClient: Client): Check =
    new Check(None, products.toSet, paidClient, Some(LocalDateTime.now()))
}
