package tinkoff.fintech.service.storage

import tinkoff.fintech.service.data.{Check, Client}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class TrieMapStorage extends Storage {

  var oldId = 0
  def nextID = {
    oldId += 1
    oldId
  }

  var checks: TrieMap[Int, Check] = TrieMap.empty
  var clients: TrieMap[Int, Client] = TrieMap.empty


  override implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  override def save(idOption: Option[Int], check: Check): Future[Int] = Future {
    val id = idOption.getOrElse(nextID)
    checks.put(id, check)
    id
  }

  override def findCheck(id: Int): Future[Check] = Future {
    checks(id)
  }

  override def save(idOption: Option[Int], client: Client): Future[Int] = Future {
    val id = idOption.getOrElse(nextID)
    clients.put(id, client)
    id
  }

  override def findClient(id: Int): Future[Client] = Future {
    clients(id)
  }

}
