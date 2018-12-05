package tinkoff.fintech.service.storage

import cats.implicits._
import tinkoff.fintech.service.data.{Check, Client}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

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
  override def saveNewCheck(check: Check): Option[Check] = {
    val id = nextID
    checks += id -> check.copy(Some(id), check.products.map(_.copy(Some(nextID))))
    findCheck(id)
  }

  override def updateCheck(check: => Check): Option[Check] = {
    checks.get(check.id.get).map(_ => checks.update(check.id.get, check))
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
}
