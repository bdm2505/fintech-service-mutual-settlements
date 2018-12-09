package tinkoff.fintech.service

import java.time.LocalDateTime

import org.scalatest.{AsyncFlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, Product}
import tinkoff.fintech.service.storage.TrieMapStorage

class TreeMapStorageTest extends AsyncFlatSpec with Matchers {

  var storage = new TrieMapStorage

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


  val client1 = Client(Some(2), "", "", Some("+7999"), None)
  val client2 = Client(Some(3), "Alex", "test2@email.com", None, Some("0000 0000"))
  val client3 = Client(Some(4), "Test", "test3@email.com", None, Some("0000 0000"))

  val products = Seq(Product(Some(29), "prod1", 10, Some(client2)), Product(Some(30), "prod2", 20, Some(client2)), Product(Some(31), "prod3", 30, Some(client1)))
  val check1 = Check(Some(59), products, client1, Some(LocalDateTime.of(2018, 11, 10, 23, 23)))
  val products2 = Seq(Product(Some(29), "prod1", 10, Some(client1)), Product(Some(30), "prod2", 20, Some(client1)), Product(Some(31), "prod3", 30, Some(client1)))
  val check2 = Check(Some(59), products2, client1, Some(LocalDateTime.of(2018, 12, 10, 23, 23)))
  val products3 = Seq(Product(Some(29), "prod1", 10, Some(client3)), Product(Some(30), "prod2", 20, Some(client2)), Product(Some(31), "prod3", 30, Some(client1)))
  val check3 = Check(Some(59), products3, client1, Some(LocalDateTime.of(2018, 11, 20, 23, 23)))

  it should "correct get max product" in {
    storage = new TrieMapStorage
    storage.transact {
      for {
        _ <- storage.saveNewCheck(check1)
        _ <- storage.saveNewCheck(check2)
        _ <- storage.saveNewCheck(check3)
        max <- storage.maxProducts(client1.id.get)
      } yield max shouldBe 3
    }
  }

  it should "correct get max product for empty result" in {
    storage.transact {
      for {
        max <- storage.maxProducts(11)
      } yield max shouldBe 0
    }
  }

  it should "correct get min product" in {
    storage.transact {
      for {
        min <- storage.minProducts(client1.id.get)
      } yield min shouldBe 1
    }
  }

  it should "correct get min product for empty result" in {
    storage.transact {
      for {
        min <- storage.minProducts(11)
      } yield min shouldBe 0
    }
  }

  it should "correct get avg product" in {
    storage.transact {
      for {
        avg <- storage.avgProducts(client1.id.get)
      } yield avg shouldBe 1
    }
  }

  it should "correct get avg product for empty result" in {
    storage.transact {
      for {
        avg <- storage.avgProducts(11)
      } yield avg shouldBe 0
    }
  }

  it should "correct get max sum" in {
    storage.transact {
      for {
        max <- storage.maxSum(client1.id.get)
      } yield max shouldBe 60
    }
  }

  it should "correct get max sum for empty result" in {
    storage.transact {
      for {
        max <- storage.maxSum(11)
      } yield max shouldBe 0
    }
  }

  it should "correct get min sum" in {
    storage.transact {
      for {
        min <- storage.minSum(client1.id.get)
      } yield min shouldBe 30
    }
  }

  it should "correct get min sum for empty result" in {
    storage.transact {
      for {
        min <- storage.minSum(11)
      } yield min shouldBe 0
    }
  }

  it should "correct get avg sum" in {
    storage.transact {
      for {
        avg <- storage.avgSum(client1.id.get)
      } yield avg shouldBe 40
    }
  }

  it should "correct get avg sum for empty result" in {
    storage.transact {
      for {
        avg <- storage.avgSum(11)
      } yield avg shouldBe 0
    }
  }

  it should "correct get total products" in {
    storage.transact {
      for {
        count <- storage.totalProducts(client1.id.get)
      } yield count shouldBe 5
    }
  }

  it should "correct get total products for empty result" in {
    storage.transact {
      for {
        count <- storage.totalProducts(11)
      } yield count shouldBe 0
    }
  }

  it should "correct get sum per week" in {
    val map = Map(
      LocalDateTime.parse("2018-11-05T00:00") -> 30.0,
      LocalDateTime.parse("2018-11-19T00:00") -> 30.0,
      LocalDateTime.parse("2018-12-10T00:00") -> 60.0
    )
    storage.transact {
      for {
        result <- storage.sumPerWeek(client1.id.get)
      } yield result shouldBe map
    }
  }

  it should "correct get sum per week for empty result" in {
    storage.transact {
      for {
        result <- storage.sumPerWeek(100)
      } yield result shouldBe Map.empty
    }
  }

  it should "correct get sum per month" in {
    val map = Map(
      LocalDateTime.parse("2018-11-01T00:00") -> 60.0,
      LocalDateTime.parse("2018-12-01T00:00") -> 60.0
    )
    storage.transact {
      for {
        result <- storage.sumPerMonth(client1.id.get)
      } yield result shouldBe map
    }
  }

  it should "correct get sum per month for empty result" in {
    storage.transact {
      for {
        result <- storage.sumPerMonth(100)
      } yield result shouldBe Map.empty
    }
  }
}
