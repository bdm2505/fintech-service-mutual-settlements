package tinkoff.fintech.service.quest

import cats.{Monad, Traverse}
import cats.implicits._
import tinkoff.fintech.service.data
import tinkoff.fintech.service.data.{Check, Client, Product}
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}


class BasicWorker[F[_] : Monad ](val storage: Storage[F], val emailSender: EmailSender)(implicit ec: ExecutionContext) extends Worker {

  def work(request: Request): Future[Response] = {
    successWork(request).recover { case e: Exception => e.printStackTrace(); Fail(e.getMessage) }
  }


  def successWork(request: Request): Future[Response] = {
    import storage._
    val res: F[Response] = request match {
      case AddProducts(id, products) =>
        for {
          check <- findCheck(id)
          _ <- updateCheck(check ++ products)
        } yield Ok

      case CreateCheck(products, idPaidClient) =>
        for {
          id <- saveNewCheck(Check(products, idPaidClient))
        } yield OkCreate(id)

      case CreateClient(client) =>
        saveNewClient(client).map(id => OkCreate(id))

      case Connect(checkId, clientId, productId) =>
        for {
          check <- findCheck(checkId)
          _ <- updateCheck(check.connect(productId, clientId))
        } yield Ok

      case SendEmail(checkId) =>
        return transact {
          for {
            check <- storage.findCheck(checkId)
            paidClient <- storage.findClient(check.paidClientId)
            group = check.filterNoPaid.groupBy(_.id)
            cli <- findClients(group.keys.toList.flatten)
          } yield cli.map(client => (client.email, paidClient, group(client.id)))
        }.flatMap(emailSender.sendAll).map(_ => Ok)

    }
    transact(res)
  }

}
