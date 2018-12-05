package tinkoff.fintech.service

import org.scalatest.{AsyncFlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, Product}
import tinkoff.fintech.service.storage.TrieMapStorage

class TreeMapStorageTest extends AsyncFlatSpec with Matchers {

  val storage = new TrieMapStorage

  val client = Client(Some(1), "bob", "bob@y.ru", Some("9000"), None)
  it should "save and find client" in {
    storage.transact {
      for {
        savedClient <- storage.saveNewClient(client)
      } yield savedClient shouldBe client
    }
  }

  val check = Check(Some(2), Seq(Product(Some(3), "milk", 90, None)), client)
  it should "save ans find check" in {
    storage.transact {
      for {
        savedCheck <- storage.saveNewCheck(check)
      } yield savedCheck shouldBe check
    }
  }

  it should "update check" in {
    storage.transact {
      for {
        _ <- storage.updateCheck(check.connect(client, 1))
        foundCheck <- storage.findCheck(check.id.get)
      } yield foundCheck shouldBe check.connect(client, 1)
    }
  }

}
