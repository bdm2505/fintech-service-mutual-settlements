package tinkoff.fintech.service.quest

import cats.Monad
import cats.implicits._
import tinkoff.fintech.service.data
import tinkoff.fintech.service.data.{Check, Client}
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}


class BasicWorker[F[_] : Monad](val storage: Storage[F], val emailSender: EmailSender)(implicit ec: ExecutionContext) extends Worker {

  def work(request: Request): Future[Response] = {
    successWork(request).recover { case _: Exception => Fail }
  }


  def successWork(request: Request): Future[Response] = {
    import storage._
    val res: F[Response] = request match {
      case AddProducts(id, products) =>
        for {
          check <- findCheck(id)
          _ <- updateCheck(id, check ++ products)
        } yield Ok

      case CreateCheck(products, idPaidClient) =>
        for {
          paidClient <- findClient(idPaidClient)
          id <- saveNewCheck(Check(products, paidClient))
        } yield OkCreate(id)

      case CreateClient(client) =>
        saveNewClient(client).map(id => OkCreate(id))

      case Connect(checkId, clientId, name) =>
        for {
          client <- findClient(clientId)
          check <- findCheck(checkId)
          _ <- updateCheck(checkId, check.connect(client, name))
        } yield Ok

      case SendEmail(checkId) =>
        return transact {
          for {
            check <- storage.findCheck(checkId)
          } yield check.noPaidClients.map {
            case (client, products) => (client.email, check.paidClient, products)
          }.toSeq
        }.flatMap(emailSender.sendAll).map(_ => Ok)

    }
    transact(res)
  }

}
