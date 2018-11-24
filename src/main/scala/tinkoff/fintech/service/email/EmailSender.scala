package tinkoff.fintech.service.email

import tinkoff.fintech.service.data.Client
import tinkoff.fintech.service.data.Product

import scala.concurrent.{ExecutionContext, Future}

trait EmailSender {

  def send(email: String, paidClient: Client, products: Seq[Product]): Future[Unit]

  def sendAll(list: Seq[(String, Client, Seq[Product])])(implicit ec: ExecutionContext): Future[Seq[Unit]] =
    Future.traverse(list){ case (email, props, product) => send(email, props, product)}
}
