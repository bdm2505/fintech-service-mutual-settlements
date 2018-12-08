package tinkoff.fintech.service

import cats.implicits._
import com.typesafe.config.ConfigFactory
import tinkoff.fintech.service.email.Sender
import tinkoff.fintech.service.quest.Worker
import tinkoff.fintech.service.services.{AkkaHttpService, BotTelegramService, ConsoleService, Service}
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.io.StdIn

object Launcher extends App {
  implicit val ec = ExecutionContext.global
  val config = ConfigFactory.load()

  val storage = Storage()
  val emailSender = Sender()

  val worker = Worker(storage, emailSender)

  def loadService(name: String)(fun: => Service): Option[Service] =
    if (config.getBoolean(s"service.$name.enabled")) Some(fun) else None

  var isConsole = false

  val services = Seq(
    loadService("akka") {
      new AkkaHttpService(config.getString("service.akka.host"), config.getInt("service.akka.port"))
    },
    loadService("bot-telegram"){
      new BotTelegramService(config.getConfig("service.bot-telegram"))
    },
    loadService("console") {
      isConsole = true
      new ConsoleService()
    }).flatten

  val running = services.map(_.start(worker))

  if (!isConsole) {
    StdIn.readLine
    services.foreach(_.stop())
  } else {
    running.foreach(Await.ready(_, Duration.Inf))
  }
}
