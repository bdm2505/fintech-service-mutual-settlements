package tinkoff.fintech.service

import java.io.Writer

sealed trait Command

case class AddProduct(product: Product) extends Command

case class RemoveProduct(name: String) extends Command

case class ConnectClientToProduct(nameClient: String, nameProduct: String) extends Command

case class GetProducts(clientName: String, handler: Seq[Product] => Unit) extends Command

case class Calculate(paidClient: String) extends Command

case class AddClient(client: Client) extends Command

case class SaveClient(name: String) extends Command
case class LoadClient(name: String) extends Command

case class SaveCheck(name: String) extends Command
case class LoadCheck(name: String) extends Command

case class Close() extends Command
