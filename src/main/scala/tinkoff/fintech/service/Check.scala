package tinkoff.fintech.service


case class Check(products: Seq[Product] = Seq.empty) {

  def add(ps: Seq[Product]): Check =
    copy(products ++ ps)

  def remove(names: Seq[String]): Check =
    copy(products.filterNot(p => names.contains(p.name)))

  def find(name: String): Option[Product] =
    products.find(_.name == name)

  def +(products: Product*): Check =
    add(products)

  def -(names: String*): Check =
    remove(names)

  def ++(ps: Seq[Product]): Check =
    add(ps)

  def --(ps: Seq[String]): Check =
    remove(ps)

}




