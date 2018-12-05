package tinkoff.fintech.service

import java.sql.DriverManager

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

}
