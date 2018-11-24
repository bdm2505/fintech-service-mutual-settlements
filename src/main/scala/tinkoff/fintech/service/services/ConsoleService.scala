package tinkoff.fintech.service.services

import tinkoff.fintech.service.data.{Client, ID, Product}
import tinkoff.fintech.service.quest._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

class ConsoleService(implicit val ec:ExecutionContext) extends Service {

  override def start(worker: Worker) = Future {
    println(help)
    while (true) {
      worker.work(nextRequest).onComplete { e =>
        println(e)
      }
    }
  }

  val help: String =
    s"""|         Help:
        |create [] - created empty check
        |add [id, product-name, product-cost]
        |client [name, email, props]
        |connect [id-check, id-client, name-product]
        |paid [id-paid-client, id-check]
        |exit []
      """.stripMargin

  def nextRequest: Request = {
    val args = StdIn.readLine().filterNot("[],".contains(_)).split(" ").map(_.trim)
    try {
      args(0) match {
        case "add" =>
          AddProducts(ID(args(1)),  Seq(Product(args(2), args(3).toDouble)))
        case "create" =>
          CreateCheck(Seq.empty)
        case "client" =>
          CreateClient(Client(args(1), args(2), args(3)))
        case "connect" =>
          Connect(ID(args(1)), ID(args(2)), args(3))
        case "paid" =>
          Calculate(ID(args(1)), ID(args(2)))
        case "exit" =>
          System.exit(0)
          nextRequest

        case _ =>
          println(help)
          nextRequest
      }
    } catch {
      case _: IndexOutOfBoundsException =>
        println(help)
        nextRequest
    }
  }
}
