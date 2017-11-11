package com.dblazejewski.akka.stream

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.scalalogging.Logger
import org.scalameter._

case class StreamWork(from: Int, to: Int)

class StreamActor extends Actor {
  private val logger = Logger("StreamActor")

  override def receive: Receive = {
    case m: StreamWork =>
      val time = measure {
        logger.info(s"Processing stream work [$m]")
      }

      logger.info(s"Total time: $time")
  }
}

object AkkaBenchmark extends App {

  val system: ActorSystem = ActorSystem("akka")

  val streamActor: ActorRef = system.actorOf(Props[StreamActor], "streamWork")

  streamActor ! StreamWork(1, 1000)
}