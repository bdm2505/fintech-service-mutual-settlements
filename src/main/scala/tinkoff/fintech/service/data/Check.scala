package tinkoff.fintech.service.data

import java.time.LocalDateTime

import io.circe.generic.JsonCodec

@JsonCodec
final case class Check(
                        products: Seq[Product] = Seq.empty,
                        clients: Map[Int, List[String]] = Map.empty,
                        time: Option[LocalDateTime] = None
                      ) {

  def add(ps: Seq[Product]): Check =
    copy(products = products ++ ps)

  def connect(id: Int, nameProduct: String): Check =
    copy(clients = clients + (id -> (nameProduct :: clients.getOrElse(id, Nil))))

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





