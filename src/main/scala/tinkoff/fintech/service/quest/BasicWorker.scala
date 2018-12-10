package tinkoff.fintech.service.quest

import cats.Monad
import cats.implicits._
import tinkoff.fintech.service.data._
import tinkoff.fintech.service.email.Sender
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}

class BasicWorker[F[_] : Monad](val storage: Storage[F], val sender: Sender)(implicit ec: ExecutionContext) extends Worker {

  def work(request: Request): Future[Response] = {
    successWork(request)
      .recover { case e: Exception => e.printStackTrace(); Fail(e.getMessage) }

  }

  def successWork(request: Request): Future[Response] = {
    import storage._
    val res: F[Response] = request match {
      case AddProducts(id, products) =>
        for {
          oldCheck <- findCheck(id)
          check = oldCheck ++ products
          _ <- updateCheck(check)
        } yield OkCheck(check)

      case CreateCheck(products, idPaidClient) =>
        for {
          paidClient <- findClient(idPaidClient)
          check <- saveNewCheck(Check(products, paidClient))
        } yield OkCheck(check)

      case CreateClient(client) =>
        saveNewClient(client).map(OkClient)

      case Connect(checkId, clientId, productId) =>
        for {
          client <- findClient(clientId)
          oldCheck <- findCheck(checkId)
          check = oldCheck.connect(client, productId)
          _ = if (check.full) sendEmail(check)
          _ <- updateCheck(check)
        } yield OkCheck(check)
      case GetCheck(id) =>
        for {
          check <- findCheck(id)
        } yield OkCheck(check)
      case GetSumRerMonth(id) =>
        for {
          map <- sumPerMonth(id)
        } yield OkSumPerMouth(map.map{case (k, v) => k.toString -> v})

    }
    transact(res)
  }

  def sendEmail(check: Check): Future[Seq[Unit]] = {
    println("send Email " + check)
    sender.sendAll(check.noPaidClients.map {
      case (client, products) => (client.email, check.paidClient, products)
    }.toSeq)
  }

}
