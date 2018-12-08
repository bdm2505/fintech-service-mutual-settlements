package tinkoff.fintech.service.services

import tinkoff.fintech.service.quest.Worker

import scala.concurrent.{ExecutionContext, Future, Promise}

trait Service {

  def start(worker: Worker): Future[_]

  private val stopPromise = Promise[Unit]()

  def stop(): Unit = stopPromise.success(())

  def atStop: Future[Unit] = stopPromise.future

}
