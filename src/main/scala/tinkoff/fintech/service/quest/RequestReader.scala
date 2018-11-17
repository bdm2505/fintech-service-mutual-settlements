package tinkoff.fintech.service.quest


trait RequestReader {
  def nextRequest: Request
}
