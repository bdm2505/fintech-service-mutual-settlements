package tinkoff.fintech.service.quest

import tinkoff.fintech.service.data.Check
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}


class BasicWorker(val storage: Storage, val emailSender: EmailSender)(implicit ec: ExecutionContext) extends Worker {


  def work(request: Request): Future[Response] = request match {

    case AddProducts(id, products) =>
      storage.updateCheck(id)(_.add(products)).map(_ => Ok)

    case CreateCheck(products) =>
      storage.save(None, Check(products)).map(id => OkCreate(id))

    case CreateClient(client) =>
      storage.save(None, client).map(id => OkCreate(id))

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
            (client.email, paid, products)
          }.toSeq
        }
      } yield Ok
  }

}
