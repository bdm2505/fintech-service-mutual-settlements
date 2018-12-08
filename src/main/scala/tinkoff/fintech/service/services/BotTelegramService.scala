package tinkoff.fintech.service.services


import java.net.{InetSocketAddress, Proxy}

import com.bot4s.telegram.api.declarative.{CommandFilterMagnet, Commands}
import com.bot4s.telegram.api.{Polling, TelegramBot}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.typesafe.config.Config
import slogging._
import tinkoff.fintech.service.data.{Client, Product}
import tinkoff.fintech.service.quest._

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}


case class ClientStatus(client: Option[Int] = None, check: Option[Int] = None)

class BotTelegramService(config: Config) extends TelegramBot with Polling with Commands with Service {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  val token = config.getString("token")
  val proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getString("proxy.host"), config.getInt("proxy.port")))


  override val client = new ScalajHttpClient(token, proxy)

  val help =
    """
      |/create name-product-1 cost-product-1 ... name-product-N cost-product-N  - create new check
    """.stripMargin


  val map: TrieMap[Int, ClientStatus] = TrieMap.empty


  def createCommands(worker: Worker): Unit = {
    def toAnswer(response: Response): String = response match {
      case Fail(ms) => s"fail $ms"
      case OkClient(cl) => s"client save with id=${cl.id.getOrElse("???")} (${cl.name} ${cl.email})"
      case OkCheck(check) =>
        def id(product: Product): String = product.id.getOrElse("???").toString
        def cl(pr: Product): String = pr.client.map(_.name).getOrElse("")
        s"""
           |*check update!*
           |id = ${check.id.getOrElse("???")}
           |paid = ${check.paidClient.name} (${check.paidClient.email})
           |products:
           |""".stripMargin + check.products
          .map(p => s"${id(p)}) ${p.name} ${"-" * (30 - id(p).length - p.name.length - p.cost.toString.length)} ${p.cost} (${cl(p)}) ")
          .mkString("  ", "\n  ", "\n")
    }


    def answer(filter: CommandFilterMagnet)(toRequest: (ClientStatus, Seq[String]) => Option[Request]) =
      onCommand(filter) { implicit msg =>
        val key = msg.from.get.id
        val status = map.getOrElse(key, ClientStatus())
        val sendStatus = toRequest(status, msg.text.get.split(" ").tail.toSeq).map { request =>
          worker.work(request)
            .map {
              case r@OkClient(cl) => map.update(key, status.copy(cl.id)); r
              case r@OkCheck(ch) => map.update(key, status.copy(check = ch.id)); r
              case r => r
            }
            .map(toAnswer)
            .map(ans => reply(ans))
        }
        if (sendStatus.isEmpty)
          reply(s"client id = ${status.client.getOrElse("???")}\ncheck id = ${status.check.getOrElse("???")}\n$help")
      }

    onCommand('start | 'help) { implicit msg =>
      reply(help)
    }
    onCommand('getLink) { implicit msg =>
      reply(s"link ${map.getOrElse(msg.from.get.id, ClientStatus()).check.getOrElse("???")}")
    }
    answer('create) { case (status, seq) =>
      status.client.map { id =>
        CreateCheck(for (i <- 0 until(seq.length, 2)) yield Product(seq(i), seq(i + 1).toDouble), id)
      }
    }
    answer('client) { case (status, seq) =>
      if (seq.length >= 2) {
        val lifted = seq.lift
        Some(CreateClient(Client(seq(0), seq(1), lifted(2), lifted(3))))
      } else None
    }
    answer('add) { case (status, seq) =>
      status.check.map { id =>
        AddProducts(id, for (i <- 0 until(seq.length, 2)) yield Product(seq(i), seq(i + 1).toDouble))
      }
    }
    answer('link) { case (status, seq) =>
      if (seq.size == 1)
        Some(GetCheck(seq.head.toInt))
      else None
    }
    answer('choose) { case (status, seq) =>
      for {
        idClient <- status.client
        idCheck <- status.check
        idProduct = seq.head.toInt if seq.nonEmpty
      } yield Connect(idCheck, idClient, idProduct)
    }

  }

  override def start(worker: Worker): Future[Unit] = {
    createCommands(worker)
    run()
  }


}
