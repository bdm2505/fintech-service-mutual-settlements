package tinkoff.fintech.service.storage

import java.time.LocalDateTime

import cats.{Monad, Traverse}
import cats.implicits._
import tinkoff.fintech.service.data.{Check, Client}

import scala.concurrent.Future

abstract class Storage[F[_] : Monad](implicit tr: Traverse[List]) {

  /**
    * @return context with id check
    */
  def saveNewCheck(check: Check): F[Check]

  def updateCheck(check: => Check): F[Check]

  def findCheck(id: Int): F[Check]

  /**
    * @return context with id client
    */
  def saveNewClient(client: Client): F[Client]

  def findClient(id: Int): F[Client]

  def findClients(ids: List[Int]): F[List[Client]] =
    Traverse[List].traverse(ids)(findClient)

  def transact[A](context: => F[A]): Future[A]

  def sumPerMonth(clientId: Int): F[Map[LocalDateTime, Double]]

  def sumPerWeek(clientId: Int): F[Map[LocalDateTime, Double]]

  def totalProducts(clientId: Int): F[Int]

  def maxSum(clientId: Int): F[Double]

  def minSum(clientId: Int): F[Double]

  def avgSum(clientId: Int): F[Double]

  def maxProducts(clientId: Int): F[Int]

  def minProducts(clientId: Int): F[Int]

  def avgProducts(clientId: Int): F[Int]
}

object Storage {
  def apply(): Storage[Option] = new TrieMapStorage()
}
