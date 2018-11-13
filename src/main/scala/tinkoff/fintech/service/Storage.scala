package tinkoff.fintech.service

trait Storage {

  def save(id: String, check: Check): Unit

  def findCheck(id: String): Option[Check]

  def save(id: String, client: Client): Unit

  def findClient(id: String): Option[Client]
}
