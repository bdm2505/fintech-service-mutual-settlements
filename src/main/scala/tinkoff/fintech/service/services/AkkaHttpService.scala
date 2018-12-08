package tinkoff.fintech.service.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.ActorMaterializer
import tinkoff.fintech.service.quest._
import tinkoff.fintech.service.services.AkkaHttpSupport._

import scala.util.{Failure, Success}

class AkkaHttpService(val host: String, val port: Int) extends Service {


  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher


  override def start(worker: Worker) = {

    def answer[T <: Request : FromRequestUnmarshaller]: Route = post {
      entity(as[T])(request => complete(worker.work(request)))
    }

    val route =
      path("create-check")(answer[CreateCheck]) ~
        path("add-products")(answer[AddProducts]) ~
        path("create-client")(answer[CreateClient]) ~
        path("connect")(answer[Connect]) ~
        path("get-check")(answer[GetCheck])


    val bind = Http().bindAndHandle(route, host, port)

    bind.onComplete {
      case Success(host) =>
        println(s"akka http service online at ${host.localAddress}")
      case Failure(e) =>
        println(s"akka http service fail $e")
    }

    atStop.map { _ =>
      bind.flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }
    system.whenTerminated
  }
}
