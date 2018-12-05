package tinkoff.fintech.service.email

import com.typesafe.config.{Config, ConfigFactory}
import courier._
import javax.mail.internet.InternetAddress
import tinkoff.fintech.service.data._

import scala.concurrent.Future

class EmailSender extends Sender {
  val config: Config = ConfigFactory.load().getConfig("email")

  override def send(email: String, paidClient: Client, products: Seq[Product]): Future[Unit] = {
    val mailer = Mailer(config.getString("host"), config.getInt("port"))
      .auth(true)
      .as(config.getString("user"), config.getString("pass"))
      .startTls(true)()

    mailer(Envelope.from(new InternetAddress(config.getString("user"), "Сервис взаиморасчетов"))
      .to(email.addr)
      .subject("Ваш чек")
      .content(Text(formatMessage(paidClient, products))))
  }

  def formatMessage(paidClient: Client, products: Seq[Product]): String = {
    val requisites = List(("Телефон", paidClient.phone), ("Номер карты", paidClient.cardNumber)).filter(_._2.nonEmpty)
    val productsStr = products.map(p => s"${p.name} \t ${p.cost}").mkString("\n")

    if (requisites.nonEmpty) {
      val requisitesStr = requisites.map(requisite => s"${requisite._1}: ${requisite._2.get}").mkString("\n")
      s"""$productsStr
         |Реквизиты для оплаты:
         |$requisitesStr
         |""".stripMargin
    } else
      productsStr
  }
}
