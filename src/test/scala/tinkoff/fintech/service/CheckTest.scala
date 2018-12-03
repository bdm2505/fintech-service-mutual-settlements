package tinkoff.fintech.service

import org.scalatest.{FlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, Product}

class CheckTest extends FlatSpec with Matchers {

  trait Milk {
    val check = Check(Some(1), Seq(Product(Some(1), "milk", 90)), Client("", "", None, None))
  }

  it should "add Products" in new Milk {
    check.products shouldBe Seq(Product(Some(1), "milk", 90))
  }

  it should "remove products" in new Milk {
    (check - "milk").products shouldBe Seq.empty
  }

  it should "find product" in new Milk {
    check.find("milk") shouldBe Some(Product(Some(1), "milk", 90))
    check.find("other") shouldBe None
  }

  it should "connect client and product" in new Milk {
    val cl = Client(" ", "", None, None)
    check.connect(cl, "milk").clients shouldBe Map(cl -> (Product(Some(1), "milk", 90) :: Nil))
  }
}
