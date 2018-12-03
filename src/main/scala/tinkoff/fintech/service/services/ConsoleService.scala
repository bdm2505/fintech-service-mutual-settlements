package tinkoff.fintech.service.services

import tinkoff.fintech.service.data.{Client, Product}
import tinkoff.fintech.service.quest._

import scala.concurrent.{ExecutionContext, Future}
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
        |create [id-paid-client, [product-name, product-cost]*] - created check
        |add [id, [product-name, product-cost]*] - add products in check
        |client [name, email, ?phone, ?number-card]
        |connect [id-check, id-client, id-product]
        |send-email [id-check]
        |exit []
      """.stripMargin

  def parseId[T](s: String): Int = {
    if (s.startsWith(":"))
      ids(s.tail.toInt)
    else
      s.toInt
  }

  def nextRequest: Request = {
    val args = StdIn.readLine().filterNot("[],?()'".contains(_)).split(" ").filter(_.length > 0).map(_.trim)
    def readProducts(startIndex: Int = 2) =
      startIndex until (args.length, 2) map (i => Product(args(i), args(i + 1).toDouble))
    try {
      args(0) match {
        case "add" =>
          AddProducts(parseId(args(1)), readProducts())
        case "create" =>
          CreateCheck(readProducts(), parseId(args(1)))
        case "client" =>
          val optArgs = args.lift
          CreateClient(Client(args(1), args(2), optArgs(3), optArgs(4)))
        case "connect" =>
          Connect(parseId(args(1)), parseId(args(2)), args(3).toInt)
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
      case e: IndexOutOfBoundsException =>
        println("error " + e)
        println(help)
        nextRequest
    }
  }
}
