package tinkoff.fintech.service

import scala.util.Random

object IDCreator {
  type ID = String

  def next: ID = 1 to 5 map (_ => (Random.nextInt(26).toChar + 'a') toChar) mkString
}
