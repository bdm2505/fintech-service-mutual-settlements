package tinkoff.fintech.service


import tinkoff.fintech.service.data.{Check, Client, ID}
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.quest._
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}

class Worker(val storage: Storage, val emailSender: EmailSender)(implicit ec: ExecutionContext) {


  def work(request: Request): Future[Response] = request match {

    case AddProducts(id, products) =>
      storage.updateCheck(id)(_.add(products)).map(_ => Ok)

    case CreateCheck(products) =>
      val id = ID.next[Check]
      storage.save(id, Check(products)).map(_ => OkCreate(id))

    case CreateClient(client) =>
      val id = ID.next[Client]
      storage.save(id, client).map(_ => OkCreate(id))

    case Connect(checkId, clientId, name) =>
      storage.updateCheck(checkId)(_.connect(clientId, name)).map(_ => Ok)

    case Calculate(paidClientId, checkId) =>
      for {
        listClients <- storage.formClientData(checkId)
        paid <- storage.findClient(paidClientId)
        _ <- emailSender.sendAll {
          listClients.filter {
            case (client, _) => client != paid
          }.map { case (client, products) =>
            (client.email, paid.props, products)
          }.toSeq
        }
      } yield Ok
  }

}
