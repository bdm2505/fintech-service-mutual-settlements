package tinkoff.fintech.service


import org.scalatest.{AsyncFlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, Product}
import tinkoff.fintech.service.storage.Storage


class StorageTest extends AsyncFlatSpec with Matchers {


  val check = Check(Seq(Product("milk", 90)), 4)
  val client = Client("bob", "bob@y.ru", Some("9000"))


  it should "save ans find check" in {
    val storage = Storage()
    storage.transact {
      for {
        id <- storage saveNewCheck check
        res <- storage findCheck id
      } yield res shouldBe check
    }
  }

//  it should "update check" in {
//    val storage = Storage()
//    storage.transact {
//      for {
//        id <- storage saveNewCheck check
//        _ <- storage.updateCheck(check - "milk")
//        res <- storage findCheck id
//      } yield res shouldBe Check(Seq(Product("milk", 90)), 4)
//    }
//  }

  it should "save and find client" in {
    val storage = Storage()
    storage.transact {
      for {
        id <- storage saveNewClient client
        res <- storage findClient id
      } yield res shouldBe client
    }
  }

}
