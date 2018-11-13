package tinkoff.fintech.service

object ConsoleApi extends App {
  val storage = new Storage {
    override def save(id: String, check: Check): Unit = println(s"save $id, $check")

    override def findCheck(id: String): Option[Check] = None

    override def save(id: String, client: Client): Unit = println(s"save $id, $client")

    override def findClient(id: String): Option[Client] = None
  }

  val emailSender = new EmailSender {
    override def send(email: String, props: String, products: Seq[Product]): Unit =
      println(s"send $email\n  props=$props\n    ${products.mkString("\n    ")}\n  sum cost=${products.map(_.cost).sum}")
  }

  val worker = new Worker(storage, emailSender)
  val reader = new ConsoleCommandReader
  println(reader.help)
  while (true) {
    val command = reader.nextCommand
    if (command == Close())
      System.exit(0)

    worker.work(command)
    println("ok!")
  }

}
