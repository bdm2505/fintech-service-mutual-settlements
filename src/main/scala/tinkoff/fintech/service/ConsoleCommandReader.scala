package tinkoff.fintech.service

import java.util.Scanner

import scala.io.StdIn

class ConsoleCommandReader extends CommandReader {


  val help: String =
    s"""|         Help:
        |add-client [name email props]
        |add [product-name product-cost]
        |connect [client-name product-name]
        |calculate [name-paid-client]
        |exit []
      """.stripMargin

  override def nextCommand: Command = {
    val args = StdIn.readLine().filterNot("[]".contains(_)).split(" ")
    try {
      args(0) match {
        case "add-client" =>
          AddClient(Client(args(1), args(2), args(3)))
        case "add" =>
          AddProduct(Product(args(1), args(2).toDouble))
        case "connect" =>
          ConnectClientToProduct(args(1), args(2))
        case "calculate" =>
          Calculate(args(1))
        case "exit" =>
          Close()
        case _ =>
          println(help)
          nextCommand
      }
    } catch {
      case _: IndexOutOfBoundsException =>
        println(help)
        nextCommand
    }
  }
}
