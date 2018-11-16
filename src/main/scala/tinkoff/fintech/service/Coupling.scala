package tinkoff.fintech.service

import tinkoff.fintech.service.IDCreator.ID

case class Coupling(check: ID, names: Map[ID, List[String]])
