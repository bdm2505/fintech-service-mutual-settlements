package tinkoff.fintech.service.storage

import tinkoff.fintech.service.data.{Check, Client, ID, Product}

import scala.concurrent.{ExecutionContext, Future}


trait Storage {

  implicit val ec: ExecutionContext

  def save(id: ID[Check], check: Check): Future[Unit]

  def findCheck(id: ID[Check]): Future[Check]

  def save(id: ID[Client], client: Client): Future[Unit]

  def findClient(id: ID[Client]): Future[Client]


  def updateCheck(id: ID[Check])(funUpdate: Check => Check): Future[Unit] =
    findCheck(id).flatMap(ch => save(id, funUpdate(ch)))

  def formClientData(id: ID[Check]): Future[Map[Client, Seq[Product]]] = {
    for {
      check <- findCheck(id)
      listClients <- Future.sequence(check.clients.map { case (k, v) =>
        for {
          client <- findClient(k)
          list = v.map(check.find).filter(_.isDefined).map(_.get)
        } yield (client, list)
      })
    } yield listClients.toMap
  }
}

object Storage {
  def apply(): Storage = new TrieMapStorage()
}
