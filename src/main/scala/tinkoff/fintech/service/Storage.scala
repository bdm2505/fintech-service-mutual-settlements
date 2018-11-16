package tinkoff.fintech.service

import tinkoff.fintech.service.IDCreator.ID

import scala.concurrent.Future

trait Storage {



  def save(id: ID, check: Check): Future[Unit]

  def updateCheck(id: ID, check: Check): Future[Unit]

  def findCheck(id: ID): Future[Check]

  def save(id: ID, client: Client): Future[Unit]

  def findClient(id: ID): Future[Client]

  def save(id: ID, coupling: Coupling): Future[Unit]

  def findCoupling(id: ID): Future[Coupling]

  def updateCoupling(id: ID, coupling: Coupling) : Future[Unit]
}

object Storage {
  def apply(): Storage = new TrieMapStorage()
}
