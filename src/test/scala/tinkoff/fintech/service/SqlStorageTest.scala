package tinkoff.fintech.service

import java.sql.DriverManager
import java.time.LocalDateTime

import cats.effect.IO
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.scalatest.{AsyncFlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, Product}
import tinkoff.fintech.service.storage.SqlStorage

class SqlStorageTest extends AsyncFlatSpec with Matchers with ForAllTestContainer {

  override val container = PostgreSQLContainer()

  lazy val storage: SqlStorage = new SqlStorage {
    override val transactor: Aux[IO, Unit] = {
      Transactor.fromDriverManager[IO](
        container.driverClassName,
        container.jdbcUrl,
        container.username,
        container.password
      )
    }
  }

  it should "container started" in {
    Class.forName(container.driverClassName)
    val connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)
    connection.createStatement().execute("SELECT 1") shouldEqual true
  }

  it should "create tables" in {
    storage.transact {
      for {
        num <- storage.createTables
      } yield num shouldBe 0
    }
  }

  val client = Client(Some(1), "bob", "bob@y.ru", Some("9000"), None)
  it should "save and find client" in {
    storage.transact {
      for {
        savedClient <- storage.saveNewClient(client)
      } yield savedClient shouldBe client
    }
  }

  val check = Check(Some(1), Seq(Product(Some(1), "milk", 90, None)), client)
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


  val client1 = Client(Some(1), "", "", Some("+7999"), None)
  val client2 = Client(Some(2), "Alex", "test2@email.com", None, Some("0000 0000"))
  val client3 = Client(Some(3), "Test", "test3@email.com", None, Some("0000 0000"))

  val products = Seq(Product(Some(1), "prod1", 10, Some(client2)), Product(Some(2), "prod2", 20, Some(client2)), Product(Some(3), "prod3", 30, Some(client1)))
  val check1 = Check(None, products, client1, Some(LocalDateTime.of(2018, 11, 10, 23, 23)))
  val products2 = Seq(Product(Some(4), "prod1", 10, Some(client1)), Product(Some(5), "prod2", 20, Some(client1)), Product(Some(6), "prod3", 30, Some(client1)))
  val check2 = Check(None, products2, client1, Some(LocalDateTime.of(2018, 12, 10, 23, 23)))
  val products3 = Seq(Product(Some(7), "prod1", 10, Some(client3)), Product(Some(8), "prod2", 20, Some(client2)), Product(Some(9), "prod3", 30, Some(client1)))
  val check3 = Check(None, products3, client1, Some(LocalDateTime.of(2018, 11, 20, 23, 23)))

  it should "correct get max product" in {
    storage.transact {
      for {
        _ <- storage.dropTables
        _ <- storage.createTables
        c1 <- storage.saveNewClient(client1)
        c2 <- storage.saveNewClient(client2)
        c3 <- storage.saveNewClient(client3)
        cc1 <- storage.saveNewCheck(check1)
        cc2 <- storage.saveNewCheck(check2)
        cc3 <- storage.saveNewCheck(check3)
        up1 <- storage.updateCheck(cc1.connect(client2, 1).connect(client2, 2).connect(client1, 3))
        up2 <- storage.updateCheck(cc2.connect(client1, 4).connect(client1, 5).connect(client1, 6))
        up3 <- storage.updateCheck(cc3.connect(client3, 7).connect(client2, 8).connect(client1, 9))
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
        result <- storage.sumPerWeek(11)
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
        result <- storage.sumPerMonth(11)
      } yield result shouldBe Map.empty
    }
  }

}
