package tinkoff.fintech.service.quest

import cats.Monad
import tinkoff.fintech.service.email.Sender
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}

trait Worker {
  def work(request: Request): Future[Response]
}

object Worker {
  def apply[F[_] : Monad](storage: Storage[F], sender: Sender)(implicit ex: ExecutionContext): Worker =
    new BasicWorker(storage, sender)
}

