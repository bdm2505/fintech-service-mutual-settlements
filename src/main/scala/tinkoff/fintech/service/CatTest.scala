package tinkoff.fintech.service

import cats.Monad
import cats.implicits._

object CatTest extends App {

  class A[F[_] : Monad]() {
    def ub(v: F[String]): F[Int] =
      v.map(s => s.length)



  }



}
