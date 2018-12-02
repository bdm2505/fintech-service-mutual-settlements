package tinkoff.fintech.service.storage

import cats.Monad
import tinkoff.fintech.service.data.{Check, Client}

import scala.concurrent.Future

abstract class Storage[F[_] : Monad] {

  /**
    * @return context with id check
    */
  def saveNewCheck(check: Check): F[Int]

  def updateCheck(check: => Check): F[Unit]

  def findCheck(id: Int): F[Check]

  /**
    * @return context with id client
    */
  def saveNewClient(client: Client): F[Int]

  def findClient(id: Int): F[Client]

  def transact[A](context: => F[A]): Future[A]

}

object Storage {
  def apply(): Storage[Option] = new TrieMapStorage()
}
