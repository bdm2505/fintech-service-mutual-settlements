package tinkoff.fintech.service.services

import tinkoff.fintech.service.quest.Worker

trait Service {

  def start(worker: Worker): Unit

}
