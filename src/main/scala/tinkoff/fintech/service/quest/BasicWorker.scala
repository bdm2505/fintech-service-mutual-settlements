package tinkoff.fintech.service.quest

import tinkoff.fintech.service.data.Check
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}


class BasicWorker(val storage: Storage, val emailSender: EmailSender)(implicit ec: ExecutionContext) extends Worker {


  def work(request: Request): Future[Response] = request match {

    case AddProducts(id, products) =>
      storage.updateCheck(id)(_.add(products)).map(_ => Ok)

    case CreateCheck(products, idPaidClient) =>
      for {
        paidClient <- storage.findClient(idPaidClient)
        id <- storage.save(None, Check(products, paidClient))
      } yield OkCreate(id)

    case CreateClient(client) =>
      storage.save(None, client).map(id => OkCreate(id))

    case Connect(checkId, clientId, name) =>
      for {
        client <- storage.findClient(clientId)
        _ <- storage.updateCheck(checkId)(_.connect(client, name))
      } yield Ok

    case SendEmail(checkId) =>


      for {
        check <- storage.findCheck(checkId)
        list = check.noPaidClients.map{case (client, l) => (client.email, check.paidClient, l)}.toSeq
        _ <- emailSender.sendAll(list)
      } yield Ok
  }

}
