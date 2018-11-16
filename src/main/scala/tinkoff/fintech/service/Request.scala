package tinkoff.fintech.service

import java.io.Writer

import tinkoff.fintech.service.IDCreator.ID

sealed trait Request

case class AddProducts(checkId: ID, product: Seq[Product]) extends Request

case class CreateCheck(products: Seq[Product]) extends Request

case class CreateClient(client: Client) extends Request

case class CreateCoupling(clientId: ID, checkId: ID, nameProduct: String) extends Request

case class Calculate(paidClientId: ID, chechId: ID) extends Request
