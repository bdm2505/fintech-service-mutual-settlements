package tinkoff.fintech.service.quest

import tinkoff.fintech.service.data.ID

sealed trait Response

case object Ok extends Response

case object Fail extends Response

case class OkCreate[T](id: ID[T]) extends Response