package tinkoff.fintech.service.storage

import cats.effect.{ContextShift, IO}
import com.typesafe.config.{Config, ConfigFactory}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object CreateTableSql extends App {
  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val transactor: Aux[IO, Unit] = {
    val config: Config = ConfigFactory.load()
    val dbConfig: Config = config.getConfig("db")
    Transactor.fromDriverManager[IO](
      dbConfig.getString("driver"),
      dbConfig.getString("url"),
      dbConfig.getString("user"),
      dbConfig.getString("pass")
    )
  }

  val q =
    sql"""
         CREATE TABLE public.client (
           id serial NOT NULL,
           name varchar NOT NULL,
           email varchar NOT NULL,
           phone varchar NULL,
           card_number varchar NULL,
           CONSTRAINT client_pkey PRIMARY KEY (id)
         );

 CREATE TABLE public."check" (
           id serial NOT NULL,
           "time" timestamp NOT NULL,
           client_id int4 NOT NULL,
           CONSTRAINT check_pkey PRIMARY KEY (id),
           CONSTRAINT check_client_fk FOREIGN KEY (client_id) REFERENCES client(id)
         );

 CREATE TABLE public.product (
           id serial NOT NULL,
           name varchar NOT NULL,
           cost numeric(10,2) NOT NULL,
           check_id int4 NOT NULL,
           client_id int4 NULL,
           CONSTRAINT product_pkey PRIMARY KEY (id),
           CONSTRAINT product_check_fk FOREIGN KEY (check_id) REFERENCES "check"(id) ON DELETE CASCADE,
           CONSTRAINT product_client_fk FOREIGN KEY (client_id) REFERENCES client(id)
         );""".update.run.transact(transactor).unsafeToFuture()

  q.onComplete {
    case Success(e) => println("ok " + e)
    case Failure(e) => e.printStackTrace()
  }

  Await.ready(q, Duration.Inf)
  Thread.sleep(2000)
}
