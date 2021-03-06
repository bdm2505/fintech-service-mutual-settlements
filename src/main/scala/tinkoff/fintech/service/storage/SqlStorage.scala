package tinkoff.fintech.service.storage

import java.sql.Timestamp
import java.time.LocalDateTime

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.update.Update
import tinkoff.fintech.service.data._

import scala.concurrent.{ExecutionContext, Future}

class SqlStorage extends Storage[ConnectionIO] {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val transactor: Aux[IO, Unit] = {
    val config: Config = ConfigFactory.load()
    val dbConfig: Config = config.getConfig("db")
    Transactor.fromDriverManager[IO](
      dbConfig.getString("driver"),
      dbConfig.getString("url"),
      dbConfig.getString("user"),
      dbConfig.getString("pass")
    )
  }

  /**
    * @return context with id check
    */
  override def saveNewCheck(check: Check): ConnectionIO[Check] = {
    val time = check.time.map(time => Timestamp.valueOf(time))

    def saveCheck =
      sql"""INSERT INTO "check" (time, client_id) VALUES ($time, ${check.paidClient.id})"""
        .update
        .withUniqueGeneratedKeys[Int]("id")

    def saveProducts(checkId: Int) = {
      val sql = "INSERT INTO product (name, cost, check_id) VALUES (?, ?, ?)"
      Update[(String, Double, Int)](sql).updateMany(check.products.map(product => (product.name, product.cost, checkId)).toList)
    }

    for {
      savedCheckId <- saveCheck
      _ <- saveProducts(savedCheckId)
      found <- findCheck(savedCheckId)
    } yield found
  }

  override def updateCheck(check: => Check): ConnectionIO[Check] = {
    val time = check.time.map(time => Timestamp.valueOf(time))

    def saveCheck =
      sql"""UPDATE "check" SET time = $time, client_id = ${check.paidClient.id} WHERE id = ${check.id}"""
        .update
        .withUniqueGeneratedKeys[Int]("id")

    def saveProducts(products: Seq[Product]) = {
      val sql = "UPDATE product SET name = ?, cost = ?, client_id = ? WHERE id = ?"
      Update[(String, Double, Option[Int], Int)](sql).updateMany(products.map(product => (product.name, product.cost, product.client.map(_.id.get), product.id.get)).toList)
    }

    for {
      savedCheckId <- saveCheck
      _ <- saveProducts(check.products)
      found <- findCheck(savedCheckId)
    } yield found
  }

  override def findCheck(id: Int): ConnectionIO[Check] = {
    def findProducts(checkId: Int) =
      sql"""
           SELECT product.id, product.name, product.cost, client.id, client.name, client.email, client.phone, client.card_number
           FROM product
           LEFT JOIN client ON client.id = client_id
           WHERE check_id = $checkId
         """
        .query[(Option[Int], String, Double, Option[Int], Option[String], Option[String], Option[String], Option[String])]
        .to[Seq]
        .map(_.map {
          case (pId, pName, pCost, cId, cName, cEmail, cPhone, cCard) =>
            val client = if (cId.isEmpty) None else Some(Client(cId, cName.get, cEmail.get, cPhone, cCard))
            new Product(pId, pName, pCost, client)
        })

    sql"""SELECT id, time, client_id FROM "check" WHERE id = $id"""
      .query[CheckBase]
      .unique
      .flatMap { check =>
        for {
          paidClient <- findClient(check.clientId)
          products <- findProducts(check.id)
        } yield Check(Some(check.id), products, paidClient, Some(check.time.toLocalDateTime))
      }
  }

  /**
    * @return db context with id client
    */
  override def saveNewClient(client: Client): ConnectionIO[Client] = {
    sql"INSERT INTO client (name, email, phone, card_number) VALUES (${client.name}, ${client.email}, ${client.phone}, ${client.cardNumber})"
      .update
      .withUniqueGeneratedKeys[Client]("id", "name", "email", "phone", "card_number")
  }

  override def findClient(id: Int): ConnectionIO[Client] =
    sql"SELECT id, name, email, phone, card_number FROM client WHERE id = $id"
      .query[Client]
      .unique

  override def transact[A](context: => ConnectionIO[A]): Future[A] =
    context.transact(transactor).unsafeToFuture()

  override def sumPerMonth(clientId: Int): ConnectionIO[Map[LocalDateTime, Double]] =
    sumPer(clientId, "month")

  override def sumPerWeek(clientId: Int): ConnectionIO[Map[LocalDateTime, Double]] =
    sumPer(clientId, "week")

  private def sumPer(clientId: Int, period: String): ConnectionIO[Map[LocalDateTime, Double]] =
    sql"""
         SELECT date_trunc($period, "check"."time"), SUM(product.cost)
         FROM product
         LEFT JOIN "check" ON product.check_id = "check".id
         WHERE product.client_id = $clientId
         GROUP BY 1
         ORDER BY 1
       """
      .query[(Timestamp, Double)]
      .to[Seq]
      .map(seq => seq.map(tuple => (tuple._1.toLocalDateTime, tuple._2)).toMap)

  override def totalProducts(clientId: Int): ConnectionIO[Int] =
    sql"SELECT COUNT(*) FROM product WHERE client_id = $clientId"
      .query[Int]
      .unique

  override def maxSum(clientId: Int): ConnectionIO[Double] =
    sum(clientId, "MAX")

  override def minSum(clientId: Int): ConnectionIO[Double] =
    sum(clientId, "MIN")

  override def avgSum(clientId: Int): ConnectionIO[Double] =
    sum(clientId, "AVG")

  private def sum(clientId: Int, op: String): ConnectionIO[Double] = {
    val sql =
      s"""
         |SELECT $op(s) FROM (
         |  SELECT SUM(product.cost) AS s
         |  FROM product
         |  LEFT JOIN "check" ON product.check_id = "check".id
         |  WHERE product.client_id = $clientId
         |  GROUP BY check_id
         |) q
       """.stripMargin
    Query0[Option[Double]](sql, None)
      .unique
      .map(_.getOrElse(0))
  }

  override def maxProducts(clientId: Int): ConnectionIO[Int] =
    products(clientId, "MAX")

  override def minProducts(clientId: Int): ConnectionIO[Int] =
    products(clientId, "MIN")

  override def avgProducts(clientId: Int): ConnectionIO[Int] =
    products(clientId, "AVG")

  private def products(clientId: Int, op: String): ConnectionIO[Int] = {
    val sql =
      s"""
         |SELECT $op(s) FROM (
         |  SELECT COUNT(*) AS s
         |  FROM product
         |  LEFT JOIN "check" ON product.check_id = "check".id
         |  WHERE product.client_id = $clientId
         |  GROUP BY check_id
         |) q
       """.stripMargin
    Query0[Option[Int]](sql, None)
      .unique
      .map(_.getOrElse(0))
  }

  def createTables: ConnectionIO[Int] =
    sql"""
         CREATE TABLE public.client (
           id serial NOT NULL,
           name varchar NOT NULL,
           email varchar NOT NULL,
           phone varchar NULL,
           card_number varchar NULL,
           CONSTRAINT client_pkey PRIMARY KEY (id)
         );

         CREATE TABLE public."check" (
           id serial NOT NULL,
           "time" timestamp NOT NULL DEFAULT now(),
           client_id int4 NOT NULL,
           CONSTRAINT check_pkey PRIMARY KEY (id),
           CONSTRAINT check_client_fk FOREIGN KEY (client_id) REFERENCES client(id)
         );

         CREATE TABLE public.product (
           id serial NOT NULL,
           name varchar NOT NULL,
           cost numeric(10,2) NOT NULL,
           check_id int4 NOT NULL,
           client_id int4 NULL,
           CONSTRAINT product_pkey PRIMARY KEY (id),
           CONSTRAINT product_check_fk FOREIGN KEY (check_id) REFERENCES "check"(id) ON DELETE CASCADE,
           CONSTRAINT product_client_fk FOREIGN KEY (client_id) REFERENCES client(id)
         );
       """
      .update
      .run

  def dropTables: ConnectionIO[Int] =
    sql"""
         DROP TABLE product;
         DROP TABLE "check";
         DROP TABLE client;
       """
      .update
      .run
}
