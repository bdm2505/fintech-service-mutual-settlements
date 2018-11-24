package tinkoff.fintech.service.services

import tinkoff.fintech.service.quest.Worker

import scala.concurrent.Future

trait Service {

  def start(worker: Worker): Future[Unit]

}
