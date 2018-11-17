package tinkoff.fintech.service.quest

import tinkoff.fintech.service.data
import tinkoff.fintech.service.data.{Check, Client, ID}


sealed trait Request

case class AddProducts(id: ID[Check], product: Seq[data.Product]) extends Request

case class CreateCheck(products: Seq[data.Product]) extends Request

case class CreateClient(client: Client) extends Request

case class Connect(checkId: ID[Check], clientId: ID[Client], nameProduct: String) extends Request

case class Calculate(paidClientId: ID[Client], checkId: ID[Check]) extends Request
