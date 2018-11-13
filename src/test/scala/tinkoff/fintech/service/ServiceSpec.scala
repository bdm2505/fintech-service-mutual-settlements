package tinkoff.fintech.service

import org.scalatest.{FlatSpec, Matchers}

class ServiceSpec extends FlatSpec with Matchers {

  "Parser" should "parse string" in {
    val s = "milk, 90\n chicken, 150.4"
    Parser.checkParse(s) shouldBe Seq(Product("milk", 90), Product("chicken", 150.4))
  }

  "Check" should "add Products" in {
    val check = Check()
    check.add(Product("milk", 90))

    check.products shouldBe Seq(Product("milk", 90))
  }
}
