package tinkoff.fintech.service.quest


sealed trait Response

case object Ok extends Response

case object Fail extends Response

case class OkCreate[T](value: T) extends Response
