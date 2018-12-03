package tinkoff.fintech.service.data

import java.sql.Timestamp
import java.time.LocalDateTime

final case class Check(
                        id: Option[Int],
                        products: Set[Product],
                        paidClientId: Int,
                        time: Option[LocalDateTime]
                      ) {

  def + (product: Product): Check =
    copy(products = products + product)

  def - (product: Product): Check =
    copy(products = products - product)

  def update(old: Product, newProduct: Product): Check =
    copy(products = products - old + newProduct)

  def connect(productId: Int, clientId: Int): Check =
    products.find(_.id.contains(productId))
      .map(product => update(product, product connect clientId))
      .getOrElse(this)

  def ++ (ps: Seq[Product]): Check =
    copy(products = products ++ ps)

  def filterNoPaid: Seq[Product] =
    products.filter(!_.clientId.contains(paidClientId)).toSeq

}
object Check{
  def apply(
             products: Seq[Product],
             paidClientId: Int,
             id: Option[Int] = None,
             time: Option[LocalDateTime] = None
           ): Check = new Check(id, products.toSet, paidClientId, time)

  def apply(
             id: Option[Int],
             products: Seq[Product],
             paidClientId: Int,
             time: Option[LocalDateTime]
           ): Check = new Check(id, products.toSet, paidClientId, time)
}

final case class CheckBase(id: Int,
                           time: Timestamp,
                           clientId: Int)
