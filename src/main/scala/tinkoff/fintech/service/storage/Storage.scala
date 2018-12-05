package tinkoff.fintech.service.storage

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

}

object Storage {
  def apply(): Storage[Option] = new TrieMapStorage()
}
