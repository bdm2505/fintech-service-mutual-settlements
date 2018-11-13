package tinkoff.fintech.service

trait EmailSender {
  def send(email: String, props: String, products: Seq[Product]): Unit
}
