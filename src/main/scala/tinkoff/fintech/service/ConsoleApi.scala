package tinkoff.fintech.service

import tinkoff.fintech.service.data.Product
import tinkoff.fintech.service.email.EmailSender
import tinkoff.fintech.service.quest.ConsoleRequestReader
import tinkoff.fintech.service.storage.TrieMapStorage

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object ConsoleApi extends App {
  val storage = new TrieMapStorage
  import storage.ec

  val emailSender = new EmailSender {
    override def send(email: String, props: String, products: Seq[Product]) = Future {
      println(s"send $email\n  props=$props\n    ${products.mkString("\n    ")}\n  sum cost=${products.map(_.cost).sum}")
    }
  }

  val worker = new Worker(storage, emailSender)
  val reader = new ConsoleRequestReader
  println(reader.help)
  while (true) {
    val request = reader.nextRequest
    worker.work(request).onComplete { e =>
      println(e)
      //println("checks=" + storage.checks)
      //println("clients=" + storage.clients)
    }
  }

}
