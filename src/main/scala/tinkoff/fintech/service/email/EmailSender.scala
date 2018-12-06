package tinkoff.fintech.service.email

import com.typesafe.config.{Config, ConfigFactory}
import courier._
import html.email
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
      .content(Multipart().html(formatMessage(paidClient, products))))
  }

  def formatMessage(paidClient: Client, products: Seq[Product]): String = {
    email.render(paidClient, products).toString()
  }
}
