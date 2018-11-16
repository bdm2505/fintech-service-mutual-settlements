package tinkoff.fintech.service


import scala.io.StdIn

class ConsoleCommandReader extends CommandReader {


  val help: String =
    s"""|         Help:
        |create [] - created empty check
        |add [id, product-name, product-cost]
        |client [name, email, props]
        |connect [id-client, id-check, name-product]
        |paid [id-paid-client, id-check]
        |exit []
      """.stripMargin

  override def nextRequest: Request = {
    val args = StdIn.readLine().filterNot("[],".contains(_)).split(" ").map(_.trim)
    try {
      args(0) match {
        case "add" =>
          AddProducts(args(1),  Seq(Product(args(2), args(3).toDouble)))
        case "create" =>
          CreateCheck(Seq.empty)
        case "client" =>
          CreateClient(Client(args(1), args(2), args(3)))
        case "connect" =>
          CreateCoupling(args(1), args(2), args(3))
        case "paid" =>
          Calculate(args(1), args(2))
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
