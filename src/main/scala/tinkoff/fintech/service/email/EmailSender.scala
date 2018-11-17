package tinkoff.fintech.service.email

import tinkoff.fintech.service.data

import scala.concurrent.{ExecutionContext, Future}

trait EmailSender {
  def send(email: String, props: String, products: Seq[data.Product]): Future[Unit]

  def sendAll(list: Seq[(String, String, Seq[data.Product])])(implicit ec: ExecutionContext): Future[Seq[Unit]] =
    Future.traverse(list){ case (email, props, products) => send(email, props, products)}
}
