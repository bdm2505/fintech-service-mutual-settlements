package tinkoff.fintech.service.data

import java.sql.Timestamp
import java.time.LocalDateTime

import io.circe._
import io.circe.syntax._


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

  def noPaidClients: Map[Client, List[Product]] =
    products
      .filter(_.client.nonEmpty)
      .map(product => (product.client, product))
      .groupBy(_._1.get).mapValues(_.map(_._2).toList)

  def connect(client: Client, productId: Int): Check = {
    copy(products = products.map(pr =>
      if (pr.id contains productId) pr connect client else pr))
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

  implicit val encoder: Encoder[Check] = (check: Check) => Json.obj(
    "id" -> check.id.asJson,
    "clients" -> check.products.flatMap(_.client).asJson,
    "products" -> check.products.map { case Product(optionId, name, cost, client) =>
      var pr: Map[String, Json] = Map(
        "name" -> name.asJson,
        "cost" -> cost.asJson
      )
      optionId foreach (id => pr += ("id" -> id.asJson))
      client map (_.id) foreach (clientId => pr += ("client-id" -> clientId.asJson))
      Json.obj(pr.toSeq: _*)
    }.asJson,
    "paid-client" -> check.paidClient.asJson,
    "time" -> check.time.asJson
  )

  implicit val decoder: Decoder[Check] = (c: HCursor) => {
    def productDecoder(clients: Map[Int, Client]): Decoder[Product] = (c: HCursor) => {
      for {
        name <- c.downField("name").as[String]
        cost <- c.downField("cost").as[Double]
        id = c.downField("id").as[Option[Int]].getOrElse(None)
        clientId = c.downField("client-id").as[Option[Int]].getOrElse(None)
      } yield Product(id, name, cost, clientId.flatMap(id => clients.get(id)))
    }

    for {
      id <- c.downField("id").as[Option[Int]]
      seq <- c.downField("clients").as[Seq[Client]]
      products <- {
        val clients = seq.filter(_.id.isDefined).map(c => (c.id.get, c)).toMap
        implicit val dec: Decoder[Product] = productDecoder(clients)
        c.downField("products").as[Seq[Product]]
      }
      paidClient <- c.downField("paid-client").as[Client]
      time <- c.downField("time").as[Option[LocalDateTime]]
    } yield Check(id, products, paidClient, time)
  }
}



