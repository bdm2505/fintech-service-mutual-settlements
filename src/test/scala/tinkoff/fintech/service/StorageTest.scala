package tinkoff.fintech.service


import org.scalatest.{AsyncFlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, Product}
import tinkoff.fintech.service.storage.Storage


class StorageTest extends AsyncFlatSpec with Matchers {


  val check = Check(Seq(Product("milk", 90)), Client("", "", ""))
  val client = Client("bob", "bob@y.ru", "9000")


  it should "save ans find check" in {
    val storage = Storage()
    for {
      id <- storage save(None, check)
      res <- storage findCheck id
    } yield res shouldBe check
  }

  it should "update check" in {
    val storage = Storage()
    for {
      id <- storage save(None, check)
      id <- storage.updateCheck(id)(_ - "milk")
      res <- storage findCheck id
    } yield res shouldBe Check(Seq(), Client("", "", ""))
  }

  it should "save and find client" in {
    val storage = Storage()
    for {
      id <- storage save(None, client)
      res <- storage findClient id
    } yield res shouldBe client
  }

}
