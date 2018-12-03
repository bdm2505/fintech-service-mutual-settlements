package tinkoff.fintech.service.services

import tinkoff.fintech.service.data.{Client, Product}
import tinkoff.fintech.service.quest._

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.Success

class ConsoleService(implicit val ec: ExecutionContext) extends Service {

  var ids = Map.empty[Int, Int]
  var num = 1

  override def start(worker: Worker): Unit = {
    println(help)
    while (true) {
      worker.work(nextRequest).onComplete {
        case Success(response) =>
          response match {
            case OkCreate(id: Int) =>
              ids += (num -> id)
              println(s"ok id = ':$num'")
              num += 1
            case e =>
              println(e)
          }
        case e => println(e)
      }
    }
  }

  val help: String =
    s"""|         Help:
        |create [id-paid-client] - created empty check
        |add [id, product-name, product-cost]
        |client [name, email, props]
        |connect [id-check, id-client, name-product]
        |send-email [id-paid-client, id-check]
        |exit []
      """.stripMargin

  def parseId[T](s: String): Int = {
    if (s.startsWith(":"))
      ids(s.tail.toInt)
    else
      s.toInt
  }

  def nextRequest: Request = {
    val args = StdIn.readLine().filterNot("[],'".contains(_)).split(" ").filter(_.length > 0).map(_.trim)
    try {
      args(0) match {
        case "add" =>
          AddProducts(parseId(args(1)), Seq(Product(args(2), args(3).toDouble)))
        case "create" =>
          CreateCheck(Seq.empty, parseId(args(1)))
        case "client" =>
          CreateClient(Client(args(1), args(2), Option(args(3)), Option(args(4))))
        case "connect" =>
          Connect(parseId(args(1)), parseId(args(2)), args(3))
        case "send-email" =>
          SendEmail(parseId(args(1)))
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
