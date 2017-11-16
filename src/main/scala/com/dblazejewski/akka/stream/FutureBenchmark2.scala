package com.dblazejewski.akka.stream

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.dblazejewski.akkaremote.model.RemoteProtocol.DoWork
import com.typesafe.scalalogging.Logger
import org.scalameter._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

package object benchmark2 {

  object Config {
    val NrOfCores = 8

    val NrOfIterations = 10000
  }

  //10 ms work
  case class SampleWork() {
    def work(value: String): String = {
      val start = System.currentTimeMillis()
      while ((System.currentTimeMillis() - start) < 10) {}
      value
    }
  }

  class StreamActorFutureSuccessful(implicit val materializer: ActorMaterializer) extends Actor {
    private val logger = Logger(classOf[StreamActorFutureSuccessful])

    override def receive: Receive = {
      case m: DoWork =>
        val time = measure {
          logger.info(s"Processing DoWork [$m]")

          val t0 = System.nanoTime()

          Source(1L to m.nrOfIterations)
            .mapAsync(Config.NrOfCores)(value => Future.successful(SampleWork().work(value.toString)))
            .runWith(Sink.ignore)
            .onComplete(_ => logger.info(s"Message processing total time: ${(System.nanoTime() - t0) / 1000000} ms"))
        }

        logger.info(s"Blocking time in actor: $time")
    }
  }

  class StreamActorFutureApply(implicit val materializer: ActorMaterializer) extends Actor {
    private val logger = Logger(classOf[StreamActorFutureApply])

    override def receive: Receive = {
      case m: DoWork =>
        val time = measure {
          logger.info(s"Processing DoWork [$m]")

          val t0 = System.nanoTime()

          Source(1L to m.nrOfIterations)
            .mapAsync(Config.NrOfCores)(value => Future {SampleWork().work(value.toString)})
            .runWith(Sink.ignore)
            .onComplete(_ => logger.info(s"Message processing total time: ${(System.nanoTime() - t0) / 1000000} ms"))
        }

        logger.info(s"Blocking time in actor: $time")
    }
  }
}

object FutureBenchmark2 extends App {
  import benchmark2._

  implicit val system: ActorSystem = ActorSystem("akka")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val streamActorFutureSuccessfulWithWork: ActorRef = system.actorOf(
    Props(new StreamActorFutureSuccessful()), "streamActorFutureSuccessful"
  )

  val streamActorFutureApplyWithWork: ActorRef = system.actorOf(
    Props(new StreamActorFutureApply()), "streamActorFutureApply"
  )

  streamActorFutureSuccessfulWithWork ! DoWork(Config.NrOfIterations)

  streamActorFutureApplyWithWork ! DoWork(Config.NrOfIterations)
}
