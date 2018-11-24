package tinkoff.fintech.service.data

import java.util.UUID

case class ID[T](value: String)

object ID {
  def next[T]: ID[T] = ID(UUID.randomUUID().toString)
}
