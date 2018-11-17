package tinkoff.fintech.service.storage

import tinkoff.fintech.service.data.{Check, Client, ID}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class TrieMapStorage extends Storage {

  var checks: TrieMap[ID[Check], Check] = TrieMap.empty
  var clients: TrieMap[ID[Client], Client] = TrieMap.empty


  override implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  override def save(id: ID[Check], check: Check): Future[Unit] = Future {
    checks.put(id, check)
  }

  override def findCheck(id: ID[Check]): Future[Check] = Future {
    checks(id)
  }

  override def save(id: ID[Client], client: Client): Future[Unit] = Future {
    clients.put(id, client)
  }

  override def findClient(id: ID[Client]): Future[Client] = Future {
    clients(id)
  }

}
