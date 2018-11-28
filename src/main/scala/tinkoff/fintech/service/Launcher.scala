package tinkoff.fintech.service

import tinkoff.fintech.service.data.Client
import tinkoff.fintech.service.data.Product
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.quest.Worker
import tinkoff.fintech.service.services.ConsoleService
import tinkoff.fintech.service.storage.TrieMapStorage

import scala.concurrent.Future

object Launcher extends App {
  val storage = new TrieMapStorage

  import storage.ec

  val emailSender = new EmailSender {
    override def send(email: String, paidClient: Client, products: Seq[Product]) = Future {
      println(s"send $email\n  props=$paidClient\n    ${products.mkString("\n    ")}\n  sum cost=${products.map(_.cost).sum}")
    }
  }

  val worker = Worker(storage, emailSender)

  new ConsoleService().start(worker)

}
