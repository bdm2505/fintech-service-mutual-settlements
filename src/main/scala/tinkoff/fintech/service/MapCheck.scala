package tinkoff.fintech.service

class MapCheck() extends Check {
  var map: Map[String, Product] = Map.empty
  var clients: Map[Client, List[Product]] = Map.empty

  override def add(product: Product): Unit =
    map += product.name -> product


  override def remove(name: String): Unit =
    map -= name

  override def connect(name: String, client: Client): Unit =
    map get name foreach { product =>
      clients += client -> (product :: clients.getOrElse(client, Nil))
    }

  override def products: Seq[Product] =
    map.values.toSeq

  override def products(client: Client): Option[Seq[Product]] =
    clients.get(client)
}
