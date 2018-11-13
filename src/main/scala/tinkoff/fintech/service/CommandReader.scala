package tinkoff.fintech.service



trait CommandReader {
  def nextCommand: Command
}
