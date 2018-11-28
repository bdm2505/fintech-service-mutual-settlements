package tinkoff.fintech.service.quest

import tinkoff.fintech.service.data
import tinkoff.fintech.service.data.Client


sealed trait Request

final case class AddProducts(id: Int, product: Seq[data.Product]) extends Request

final case class CreateCheck(products: Seq[data.Product]) extends Request

final case class CreateClient(client: Client) extends Request

final case class Connect(checkId: Int, clientId: Int, nameProduct: String) extends Request

final case class Calculate(paidClientId: Int, checkId: Int) extends Request

