package tinkoff.fintech.service

import org.scalatest.{FlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Product}

class ServiceSpec extends FlatSpec with Matchers {


  "Check" should "add Products" in {
    val check = Check() + Product("milk", 90)

    check.products shouldBe Seq(Product("milk", 90))
  }
}
