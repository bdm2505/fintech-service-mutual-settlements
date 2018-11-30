package tinkoff.fintech.service.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.ActorMaterializer
import tinkoff.fintech.service.quest._
import tinkoff.fintech.service.services.AkkaHttpSupport._

class AkkaHttpService extends Service {


  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher


  override def start(worker: Worker): Unit = {

    def answer[T <: Request : FromRequestUnmarshaller]: Route = post {
      entity(as[T])(request => complete(worker.work(request)))
    }

    val route =
        path("create-check")(answer[CreateCheck]) ~
        path("add-products")(answer[AddProducts]) ~
        path("create-client")(answer[CreateClient]) ~
        path("connect")(answer[Connect]) ~
        path("send-email")(answer[SendEmail])


    val bind = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress Return to stop...")
    atStop.map { _ =>
      bind.flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }
  }
}
