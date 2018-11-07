package tinkoff.fintech.service

case class Price(name: String, cost: Double)

case class Client(name: String, email: String, props: String)


class Api(val check: Seq[Price]) {

  var paidClient: Option[Client] = None
  var clients: Map[Client, List[Price]] = Map.empty

  def paid(client: Client): Unit =
    paidClient = Some(client)

  def choice(client: Client, namePrice: String): Unit = {
    check.find(_.name == namePrice).foreach { price =>
      clients += client -> (price :: clients.getOrElse(client, Nil))
    }
  }

  def calculate: Seq[(Client, Double, Option[Client], List[Price])] = clients
    .filter { case (c, _) => !paidClient.contains(c) }
    .map { case (c, l) => (c, l.map(_.cost).sum, paidClient, l) }
    .toSeq

}

object Api extends App {
  val pit = Client("Pit", "pit@tinkoff.ru", "8-123-0403002")
  val bob = Client("Bob", "bob@tinkoff.ru", "8-123-0403003")
  val jon = Client("Jon", "jon@tinkoff.ru", "8-123-0403004")

  val check = Parser.checkParseFromFile("checks/check1.csv")
  val api = new Api(check)

  api.paid(pit)

  api.choice(bob, "молоко")
  api.choice(bob, "суп")
  api.choice(jon, "суп")
  api.choice(jon, "молоко")
  api.choice(jon, "молоко")
  api.choice(jon, "картошка")
  api.choice(pit, "суп")

  println(api.calculate.map { case (client, cost, paidClient, listPrise) =>
        s"получатель ${client.name} (${client.email})\n" +
        s"  оплатить ${(cost * 100 round) / 100.0} рублей на счет ${paidClient.map(_.props).getOrElse("???")} (${paidClient.map(_.name).getOrElse("???")})\n" +
        s"  покупки = {\n    ${listPrise.map(p => s"${p.name} ${"-" * (30 - p.name.length)}> ${p.cost} руб.").mkString("\n    ")}\n  }"}
    .mkString("\n\n"))
  }