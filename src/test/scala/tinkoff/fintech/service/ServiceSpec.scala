package tinkoff.fintech.service

import org.scalatest.{FlatSpec, Matchers}

class ServiceSpec extends FlatSpec with Matchers {
  "Parser" should "parse string" in {
    val s = "milk, 90\n chicken, 150.4"
    Parser.checkParse(s) shouldBe Seq(Price("milk", 90), Price("chicken", 150.4))
  }
}
