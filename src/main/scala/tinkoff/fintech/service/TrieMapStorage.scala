package tinkoff.fintech.service

import tinkoff.fintech.service.IDCreator.ID

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class TrieMapStorage extends Storage {

  var checks: TrieMap[ID, Check] = TrieMap.empty
  var clients: TrieMap[ID, Client] = TrieMap.empty
  var couples: TrieMap[ID, Coupling] = TrieMap.empty

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  override def save(id: ID, check: Check): Future[Unit] = Future {
    if (checks.get(id).isDefined)
      throw new Exception(s"element with id=$id is defined")
    checks.put(id, check)
  }

  override def findCheck(id: ID): Future[Check] = Future {
    checks(id)
  }

  override def save(id: ID, client: Client): Future[Unit] = Future {
    if (clients.get(id).isDefined)
      throw new Exception(s"element with id=$id is defined")
    clients.put(id, client)
  }

  override def findClient(id: ID): Future[Client] = Future {
    clients(id)
  }

  override def save(id: ID, coupling: Coupling): Future[Unit] = Future {
    if (couples.get(id).isDefined)
      throw new Exception(s"element with id=$id is defined")
    couples.put(id, coupling)
  }

  override def findCoupling(id: ID): Future[Coupling] = Future {
    couples(id)
  }

  override def updateCheck(id: ID, check: Check): Future[Unit] = Future {
    checks.update(id, check)
  }

  override def updateCoupling(id: ID, coupling: Coupling): Future[Unit] = Future {
    couples.update(id, coupling)
  }
}
