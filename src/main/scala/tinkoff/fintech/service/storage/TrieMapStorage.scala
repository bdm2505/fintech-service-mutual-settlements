package tinkoff.fintech.service.storage

import tinkoff.fintech.service.data.{Check, Client}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._

class TrieMapStorage extends Storage[Option] {
  implicit val ec = ExecutionContext.global

  var oldId = 0
  def nextID = {
    oldId += 1
    oldId
  }

  var checks: TrieMap[Int, Check] = TrieMap.empty
  var clients: TrieMap[Int, Client] = TrieMap.empty

  /**
    * @return db context with id check
    */
  override def saveNewCheck(check: Check): Option[Int] = {
    val id = nextID
    checks += id -> check
    Some(id)
  }

  override def updateCheck(id: Int, check: => Check): Option[Unit] =
    checks.get(id).map(_ => checks.update(id, check))

  override def findCheck(id: Int): Option[Check] =
    checks.get(id)

  /**
    * @return db context with id client
    */
  override def saveNewClient(client: Client): Option[Int] = {
    val id = nextID
    clients += id -> client
    Some(id)
  }

  override def findClient(id: Int): Option[Client] =
    clients.get(id)

  override def transact[A](context: => Option[A]): Future[A] =
    Future(context.get)
}


