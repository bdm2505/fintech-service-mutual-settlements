package tinkoff.fintech.service.data

import java.sql.Timestamp
import java.time.LocalDateTime

final case class Check(id: Option[Int],
                       products: Seq[Product],
                       paidClient: Client,
                       clients: Map[Client, List[Product]] = Map.empty,
                       time: Option[LocalDateTime] = Some(LocalDateTime.now())) {

  def noPaidClients: Map[Client, List[Product]] = clients.filter(_._1 != paidClient)

  def add(ps: Seq[Product]): Check =
    copy(products = products ++ ps)

  def connect(client: Client, name: String): Check =
    find(name).map { product =>
      copy(clients = clients + (client -> (product :: clients.getOrElse(client, Nil))))
    } getOrElse this

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

//final case class TestCheck(products: Seq[Product],
//                           paidClient: Client,
//                           clients: Map[Client, List[Product]] = Map.empty,
//                           time: Option[LocalDateTime] = None)