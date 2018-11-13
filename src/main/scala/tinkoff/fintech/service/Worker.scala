package tinkoff.fintech.service

class Worker(val storage: Storage, val emailSender: EmailSender) {
  var clients: Set[Client] = Set.empty
  var check = Check()

  def findClient(name: String)(fun: Client => Unit): Unit =
    clients.find(_.name == name).foreach(fun)

  def work(command: Command): Unit = command match {
    case AddProduct(product) =>
      check += product
    case AddClient(client) =>
      clients += client
    case RemoveProduct(name) =>
      check -= name
    case ConnectClientToProduct(client, product) =>
      findClient(client) {
        check.connect(product, _)
      }
    case GetProducts(name, handler) =>
      findClient(name) {
        check.products(_).foreach(handler)
      }
    case Calculate(paidClientName) =>
      findClient(paidClientName) { paidClient =>
        clients.filter(_ != paidClient).foreach{ client =>
          check.products(client).foreach{ seq =>
            emailSender.send(client.email, paidClient.props, seq)
          }
        }
      }
    case _ => ???
  }

}
