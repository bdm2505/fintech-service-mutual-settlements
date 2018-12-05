package tinkoff.fintech.service

import org.scalatest.{FlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, Product}

class CheckTest extends FlatSpec with Matchers {

  trait Milk {
    val check = Check(Some(1), Seq(Product(Some(1), "milk", 90, None)), Client("", ""))
  }

  it should "add Products" in new Milk {
    check.products shouldBe Seq(Product(Some(1), "milk", 90, None))
  }

  it should "remove products" in new Milk {
    (check - Product(Some(1), "milk", 90, None)).products shouldBe Seq.empty
  }

  it should "connect client and product" in new Milk {
    val cl = Client(" ", "")
    check.connect(cl, 1).products shouldBe Seq(Product(Some(1), "milk", 90, Some(cl)))
  }

  it should "read and write in Json" in new Milk {
    import io.circe.syntax._
    import io.circe.parser._
     decode[Check](check.asJson.noSpaces).getOrElse("error") shouldBe check
  }
}
