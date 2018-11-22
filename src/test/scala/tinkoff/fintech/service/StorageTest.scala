package tinkoff.fintech.service


import org.scalatest.{AsyncFlatSpec, Matchers}
import tinkoff.fintech.service.data.{Check, Client, ID, Product}
import tinkoff.fintech.service.storage.Storage


class StorageTest extends AsyncFlatSpec with Matchers {


  val check = Check() + Product("milk", 90)
  val client = Client("bob", "bob@y.ru", "9000")

  val idCheck = ID[Check]("check")
  val idClient = ID[Client]("client")


  it should "save ans find check" in {
    val storage = Storage()
    for {
      _ <- storage save(idCheck, check)
      res <- storage findCheck idCheck
    } yield res shouldBe check
  }

  it should "update check" in {
    val storage = Storage()
    for {
      _ <- storage save(idCheck, check)
      _ <- storage.updateCheck(idCheck)(_ - "milk")
      res <- storage findCheck idCheck
    } yield res shouldBe Check()
  }

  it should "save and find client" in {
    val storage = Storage()
    for {
      _ <- storage save(idClient, client)
      res <- storage findClient idClient
    } yield res shouldBe client
  }

  it should "get client data" in {
    val storage = Storage()
    for {
      _ <- storage save(idCheck, check)
      _ <- storage save(idClient, client)
      _ <- storage.updateCheck(idCheck)(_ connect(idClient, "milk"))
      res <- storage formClientData idCheck
    } yield res shouldBe Map(client -> Seq(Product("milk", 90)))
  }

}
