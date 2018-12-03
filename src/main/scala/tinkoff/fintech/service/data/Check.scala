package tinkoff.fintech.service.data

import java.sql.Timestamp
import java.time.LocalDateTime

final case class Check(id: Option[Int],
                       products: Seq[Product],
                       paidClient: Client,
                       time: Option[LocalDateTime] = Some(LocalDateTime.now())) {

  def noPaidClients: Map[Client, List[Product]] =
    products
      .filter(_.client.nonEmpty)
      .map(product => (product.client, product))
      .groupBy(_._1.get).mapValues(_.map(_._2).toList)

  def add(ps: Seq[Product]): Check =
    copy(products = products ++ ps)

  def connect(client: Client, name: String): Check = {
    val (a, b) = products.partition(_.name == name)
    if (a.isEmpty)
      this
    else
      this.copy(products = a.map(_.copy(client = Some(client))) ++ b)
  }

  def remove(names: Seq[String]): Check =
    copy(products = products.filterNot(p => names.contains(p.name)))

  def find(name: String): Option[Product] =
    products.find(_.name == name)

  def +(products: Product*): Check =
    add(products)

  def -(names: String*): Check =
    remove(names)

  def ++(ps: Seq[Product]): Check =
    add(ps)

  def --(ps: Seq[String]): Check =
    remove(ps)

}

final case class CheckBase(id: Int,
                           time: Timestamp,
                           clientId: Int)

object Check {
  def apply(products: Seq[Product], paidClient: Client): Check =
    new Check(None, products, paidClient, Some(LocalDateTime.now()))
}
