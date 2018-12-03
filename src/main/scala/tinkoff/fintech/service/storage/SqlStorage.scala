package tinkoff.fintech.service.storage

import java.sql.Timestamp

import cats.Traverse
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
  override def saveNewCheck(check: Check): ConnectionIO[Int] = {
    val time = check.time.map(time => Timestamp.valueOf(time))

    def saveCheck =
      sql"""INSERT INTO "check" (time, client_id) VALUES ($time, ${check.paidClientId})"""
        .update
        .withUniqueGeneratedKeys[Int]("id")

    def saveProducts(checkId: Int) = {
      val sql = "INSERT INTO product (name, cost, check_id) VALUES (?, ?, ?)"
      Update[(String, Double, Int)](sql).updateMany(check.products.map(product => (product.name, product.cost, checkId)).toList)
    }

    for {
      savedCheck <- saveCheck
      _ <- saveProducts(savedCheck)
    } yield savedCheck
  }

  override def updateCheck(check: => Check): ConnectionIO[Unit] = {
    val time = check.time.map(time => Timestamp.valueOf(time))

    def saveCheck =
      sql"""UPDATE "check" SET time = $time, client_id = ${check.paidClientId} WHERE id = ${check.id}"""
        .update
        .run


    def saveProducts(products: Seq[Product]) = {
      val sql = "UPDATE product SET name = ?, cost = ?, client_id = ? WHERE id = ?"
      Update[(String, Double, Option[Int], Int)](sql)
        .updateMany(products.map(product => (product.name, product.cost, product.clientId, product.id.get)).toList)
    }

    for {
      _ <- saveCheck
      _ <- saveProducts(check.products.toSeq)
    } yield ()
  }

  override def findCheck(id: Int): ConnectionIO[Check] = {

    def findProducts(checkId: Int) =
      sql"""SELECT id, name, cost, client_id
            FROM product
            WHERE check_id = $checkId
        """
        .query[Product]
        .to[Seq]

    sql"""SELECT id, time, client_id FROM "check" WHERE id = $id"""
      .query[CheckBase]
      .unique
      .flatMap { check =>
        for {
          products <- findProducts(check.id)
        } yield Check(Some(check.id), products, check.clientId, Some(check.time.toLocalDateTime))
      }
  }

  /**
    * @return db context with id client
    */
  override def saveNewClient(client: Client): ConnectionIO[Int] = {
    sql"INSERT INTO client (name, email, phone, card_number) VALUES (${client.name}, ${client.email}, ${client.phone}, ${client.cardNumber})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
  }

  override def findClient(id: Int): ConnectionIO[Client] =
    sql"SELECT id, name, email, phone, card_number FROM client WHERE id = $id"
      .query[Client]
      .unique

  override def transact[A](context: => ConnectionIO[A]): Future[A] =
    context.transact(transactor).unsafeToFuture()
}

