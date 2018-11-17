package tinkoff.fintech.service.data

import scala.util.Random

case class ID[T](value: String)

object ID {
  def next[T]: ID[T] = ID(1 to 5 map (_ => (Random.nextInt(26).toChar + 'a') toChar) mkString)
}
