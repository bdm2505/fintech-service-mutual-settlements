package tinkoff.fintech.service

import com.typesafe.config.{Config, ConfigFactory}
import tinkoff.fintech.service.data.{Client, Product}
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.quest.Worker
import tinkoff.fintech.service.services.{AkkaHttpService, ConsoleService, Service}
import tinkoff.fintech.service.storage.Storage

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import cats.implicits._

object Launcher extends App {
  implicit val ec = ExecutionContext.global
  val config = ConfigFactory.load()

  val storage = Storage()

  val emailSender = new EmailSender {
    override def send(email: String, paidClient: Client, products: List[Product]) = Future {
      println(s"send $email\n  props=$paidClient\n    ${products.mkString("\n    ")}\n  sum cost=${products.map(_.cost).sum}")
    }
  }

  val worker = Worker(storage, emailSender)

  def loadService(name: String)(fun: => Service): Option[Service] =
    if (config.getBoolean(s"service.$name.enabled")) Some(fun) else None

  var isConsole = false

  val services = Seq(
    loadService("akka") {
      new AkkaHttpService(config.getString("service.akka.host"), config.getInt("service.akka.port"))
    }).flatten

  services.foreach(_.startWithFuture(worker))

  loadService("console"){ new ConsoleService()}.foreach(_.start(worker))

  StdIn.readLine
  services.foreach(_.stop())

}
