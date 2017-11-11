package com.dblazejewski.akka.stream

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.Logger
import org.scalameter._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Config {
  val NrOfCores = 8
}

case class StreamWork(from: Long, to: Long)

class StreamActorFutureSuccesfull(implicit val materializer: ActorMaterializer) extends Actor {
  private val logger = Logger(classOf[StreamActorFutureSuccesfull])

  override def receive: Receive = {
    case m: StreamWork =>
      val time = measure {
        logger.info(s"Processing stream work [$m]")

        val t0 = System.nanoTime()

        Source(m.from to m.to)
          .mapAsync(Config.NrOfCores)(_ => Future.successful("test-string"))
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

        Source(m.from to m.to)
          .mapAsync(Config.NrOfCores)(_ => Future("test-string"))
          .runWith(Sink.ignore)
          .onComplete(_ => logger.info(s"Message processing total time: ${(System.nanoTime() - t0) / 1000000} ms"))
      }

      logger.info(s"Blocking time in actor: $time")
  }
}

object AkkaBenchmark extends App {

  implicit val system: ActorSystem = ActorSystem("akka")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val streamActorFutureSuccessful: ActorRef = system.actorOf(
    Props(new StreamActorFutureSuccesfull()), "streamActorFutureSuccessful"
  )

  val streamActorFutureApply: ActorRef = system.actorOf(
    Props(new StreamActorFutureApply()), "streamActorFutureApply"
  )

  streamActorFutureSuccessful ! StreamWork(1L, 10000000L)

  streamActorFutureApply ! StreamWork(1L, 10000000L)
}