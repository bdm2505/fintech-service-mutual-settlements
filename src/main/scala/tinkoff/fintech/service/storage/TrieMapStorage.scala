package tinkoff.fintech.service.storage

import java.time.{DayOfWeek, LocalDateTime}
import java.time.temporal.ChronoUnit

import cats.implicits._
import tinkoff.fintech.service.data.{Check, Client}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class TrieMapStorage extends Storage[Option] {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  var oldId = 0

  private def nextID: Int = {
    oldId += 1
    oldId
  }

  var checks: TrieMap[Int, Check] = TrieMap.empty
  var clients: TrieMap[Int, Client] = TrieMap.empty

  /**
    * @return db context with id check
    */
  override def saveNewCheck(check: Check): Option[Check] = {
    val id = nextID
    checks += id -> check.copy(Some(id), check.products.map(_.copy(Some(nextID))))
    findCheck(id)
  }

  override def updateCheck(check: => Check): Option[Check] = {
    checks.get(check.id.get).foreach(_ => checks.update(check.id.get, check))
    findCheck(check.id.get)
  }

  override def findCheck(id: Int): Option[Check] =
    checks.get(id)

  /**
    * @return db context with id client
    */
  override def saveNewClient(client: Client): Option[Client] = {
    val id = nextID
    clients += id -> client.copy(Some(id))
    findClient(id)
  }

  override def findClient(id: Int): Option[Client] =
    clients.get(id)

  override def transact[A](context: => Option[A]): Future[A] =
    Future(context.get)

  override def sumPerMonth(clientId: Int): Option[Map[LocalDateTime, Double]] = Some(
    checks.values
      .groupBy(_.time.get.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1))
      .mapValues(_.map(_.products
        .filter(p => p.client.nonEmpty && p.client.get.id.contains(clientId))
        .map(_.cost)
        .sum).sum)
      .filter(_._2 != 0)
  )

  override def sumPerWeek(clientId: Int): Option[Map[LocalDateTime, Double]] = Some(
    checks.values
      .groupBy(_.time.get.truncatedTo(ChronoUnit.DAYS).`with`(DayOfWeek.MONDAY))
      .mapValues(_.map(_.products
        .filter(p => p.client.nonEmpty && p.client.get.id.contains(clientId))
        .map(_.cost)
        .sum).sum)
      .filter(_._2 != 0)
  )

  override def totalProducts(clientId: Int): Option[Int] = Some(
    checks.values
      .foldLeft(0) { (sum: Int, c: Check) =>
        sum + c.products.count(p => p.client.nonEmpty && p.client.get.id.contains(clientId))
      }
  )

  override def maxSum(clientId: Int): Option[Double] = Some(
    checks.values
      .map(_.products
        .filter(p => p.client.nonEmpty && p.client.get.id.contains(clientId))
        .map(_.cost)
        .sum)
      .max
  )

  override def minSum(clientId: Int): Option[Double] = Some(
    checks.values
      .map(_.products
        .filter(p => p.client.nonEmpty && p.client.get.id.contains(clientId))
        .map(_.cost)
        .sum)
      .min
  )

  override def avgSum(clientId: Int): Option[Double] = {
    val costs = checks.values
      .map(_.products
        .filter(p => p.client.nonEmpty && p.client.get.id.contains(clientId))
        .map(_.cost)
        .sum)
      .toSeq
    Some(costs.sum / costs.length)
  }

  override def maxProducts(clientId: Int): Option[Int] = Some(
    checks.values
      .map(_.products.count(p => p.client.nonEmpty && p.client.get.id.contains(clientId)))
      .max
  )

  override def minProducts(clientId: Int): Option[Int] = Some(
    checks.values
      .map(_.products.count(p => p.client.nonEmpty && p.client.get.id.contains(clientId)))
      .min
  )

  override def avgProducts(clientId: Int): Option[Int] = {
    val amount = checks.values
      .map(_.products.count(p => p.client.nonEmpty && p.client.get.id.contains(clientId)))
      .toSeq
    Some(amount.sum / amount.length)
  }
}
