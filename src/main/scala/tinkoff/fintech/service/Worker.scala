package tinkoff.fintech.service

import tinkoff.fintech.service.IDCreator.ID

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

class Worker(val storage: Storage, val emailSender: EmailSender)(implicit ec: ExecutionContext) {


  def work(request: Request): Future[Response] = request match {
    case AddProducts(id, products) =>
      for {
        check <- storage.findCheck(id)
        updateCheck = check.add(products)
        _ <- storage.save(id, updateCheck)
      } yield Ok

    case CreateCheck(products) =>
      val id = IDCreator.next
      storage.save(id, Check(products)).map(_ => OkCreate(id))

    case CreateClient(client) =>
      val id = IDCreator.next
      for {
        _ <- storage.save(id, client)
      } yield OkCreate(id)

    case CreateCoupling(clientId, checkId, name) =>
      for {
        coup <- storage.findCoupling(checkId)
        nm = name :: coup.names.getOrElse(clientId, Nil)
        _ <- storage.updateCoupling(checkId, coup.copy(names = coup.names + (clientId -> nm)))
      } yield Ok

    case Calculate(paidClientId, checkId) =>
      for {
        check <- storage.findCheck(checkId)
        coup <- storage.findCoupling(checkId)
        listClients <- Future.sequence(coup.names.map { case (k, v) =>
            for {
              client <- storage.findClient(k)
              list = v.map(check.find).filter(_.isDefined).map(_.get)
            } yield (client, list)
          })
        paid <- storage.findClient(paidClientId)
        _ <- Future.sequence(for {
          (client, list) <- listClients if client != paid
        } yield emailSender.send(client.email, paid.props, list))

      } yield Ok


    case _ => ???
  }

}
