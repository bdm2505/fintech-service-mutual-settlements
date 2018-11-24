package tinkoff.fintech.service.quest

import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}

trait Worker {
  def work(request: Request): Future[Response]
}

object Worker {
  def apply(storage: Storage, emailSender: EmailSender)(implicit ec: ExecutionContext): Worker = new BasicWorker(storage, emailSender)
}

