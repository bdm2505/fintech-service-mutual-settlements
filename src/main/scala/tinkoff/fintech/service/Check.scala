package tinkoff.fintech.service


trait Check {
  def add(product: Product): Unit

  def remove(name: String): Unit

  def connect(name: String, client: Client)

  def products: Seq[Product]

  def products(client: Client): Option[Seq[Product]]

  def +=(product: Product): Unit =
    add(product)

  def -=(name: String): Unit =
    remove(name)

  def ++=(ps: Seq[Product]): Unit =
    ps.foreach(add)

  def --=(ps: Seq[String]): Unit =
    ps.foreach(remove)
}

object Check {
  def apply(): Check = new MapCheck
}


