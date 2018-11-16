package tinkoff.fintech.service

import scala.concurrent.Future

trait EmailSender {
  def send(email: String, props: String, products: Seq[Product]): Future[Unit]
}
