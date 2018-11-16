package tinkoff.fintech.service



trait CommandReader {
  def nextRequest: Request
}
