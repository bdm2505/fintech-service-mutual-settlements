package tinkoff.fintech.service

object Parser {
  def checkParse(s: String): Seq[Product] = {
    s.split("\n").map{ line =>
      try {
        val arr = line.split(",").map(_.trim)
        Some(Product(arr(0), arr(1).toDouble))
      } catch { case _: Exception => None }
    }.filter(_.isDefined).map(_.get)
  }

  def checkParseFromFile(nameFile: String): Seq[Product] = {
    try {
      val file = io.Source.fromFile(nameFile)
      val str = file.mkString
      file.close()
      checkParse(str)
    } catch {
      case _:Exception => Seq.empty
    }
  }
}
