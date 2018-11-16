package tinkoff.fintech.service

import tinkoff.fintech.service.IDCreator.ID

sealed trait Response

case object Ok extends Response

case object Fail extends Response

case class OkCreate(id: ID) extends Response