package tinkoff.fintech.service.storage

import tinkoff.fintech.service.data.{Check, Client, Product}

import scala.concurrent.{ExecutionContext, Future}


trait Storage {

  implicit val ec: ExecutionContext

  def save(id: Option[Int], check: Check): Future[Int]

  def findCheck(id: Int): Future[Check]

  def save(id: Option[Int], client: Client): Future[Int]

  def findClient(id: Int): Future[Client]


  def updateCheck(id: Int)(funUpdate: Check => Check): Future[Int] =
    findCheck(id).flatMap(ch => save(Some(id), funUpdate(ch)))

  def formClientData(id: Int): Future[Map[Client, Seq[Product]]] = {
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
