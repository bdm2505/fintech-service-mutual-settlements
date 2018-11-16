package tinkoff.fintech.service

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object ConsoleApi extends App {
  val storage = Storage()
  implicit val ec = ExecutionContext.global

  val emailSender = new EmailSender {
    override def send(email: String, props: String, products: Seq[Product]) = Future {
      println(s"send $email\n  props=$props\n    ${products.mkString("\n    ")}\n  sum cost=${products.map(_.cost).sum}")
    }
  }

  val worker = new Worker(storage, emailSender)
  val reader = new ConsoleCommandReader
  println(reader.help)
  while (true) {
    val request = reader.nextRequest
    worker.work(request).foreach(println)
  }

}
