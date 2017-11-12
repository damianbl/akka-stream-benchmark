package com.dblazejewski.akka.stream

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.Logger
import org.scalameter._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

package object benchmark1 {

  object Config {
    val NrOfCores = 8

    val NrOfIterations = 10000000
  }

  case class StreamWork(nrOfIterations: Long)

  class StreamActorFutureSuccessful(implicit val materializer: ActorMaterializer) extends Actor {
    private val logger = Logger(classOf[StreamActorFutureSuccessful])

    override def receive: Receive = {
      case m: StreamWork =>
        val time = measure {
          logger.info(s"Processing stream work [$m]")

          val t0 = System.nanoTime()

          Source(1L to m.nrOfIterations)
            .mapAsync(Config.NrOfCores)(_ => Future.successful("just-return-a-string"))
            .runWith(Sink.ignore)
            .onComplete(_ => logger.info(s"Message processing total time: ${(System.nanoTime() - t0) / 1000000} ms"))
        }

        logger.info(s"Blocking time in actor: $time")
    }
  }

  class StreamActorFutureApply(implicit val materializer: ActorMaterializer) extends Actor {
    private val logger = Logger(classOf[StreamActorFutureApply])

    override def receive: Receive = {
      case m: StreamWork =>
        val time = measure {
          logger.info(s"Processing stream work [$m]")

          val t0 = System.nanoTime()

          Source(1L to m.nrOfIterations)
            .mapAsync(Config.NrOfCores)(_ => Future("just-return-a-string"))
            .runWith(Sink.ignore)
            .onComplete(_ => logger.info(s"Message processing total time: ${(System.nanoTime() - t0) / 1000000} ms"))
        }

        logger.info(s"Blocking time in actor: $time")
    }
  }

}

object FutureBenchmark1 extends App {

  import benchmark1._

  implicit val system: ActorSystem = ActorSystem("akka")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val streamActorFutureSuccessful: ActorRef = system.actorOf(
    Props(new StreamActorFutureSuccessful()), "streamActorFutureSuccessful"
  )

  val streamActorFutureApply: ActorRef = system.actorOf(
    Props(new StreamActorFutureApply()), "streamActorFutureApply"
  )

  streamActorFutureSuccessful ! StreamWork(Config.NrOfIterations)

  streamActorFutureApply ! StreamWork(Config.NrOfIterations)
}