package com.sortedbits.learningakka

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }

class Aphrodite extends Actor {

  import Aphrodite._

  override def preStart() = {
    println("Aphrodite preStart hook...")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    println("Aphrodite preRestart hook...")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable) = {
    println("Aphrodite postRestart hook...")
    super.postRestart(reason)
  }

  override def postStop() = {
    println("Aphrodite postStop...")
  }

  def receive = {
    case "Resume" =>
      throw ResumeException
    case "Stop" =>
      throw StopException
    case "Restart" =>
      throw RestartException
    case _ =>
      throw new Exception
  }
}

object Aphrodite {
  case object ResumeException extends Exception
  case object StopException extends Exception
  case object RestartException extends Exception
}

class Hera extends Actor {
  import Aphrodite._

  var childRef: ActorRef = _

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 second) {
      case ResumeException => Resume
      case RestartException => Restart
      case StopException => Stop
      case _: Exception => Escalate
    }

    override def preStart() = {
      childRef = context.actorOf(Props[Aphrodite], "Aphrodite")
      Thread.sleep(100)
    }

    def receive = {
      case msg =>
        println(s"Hera received ${msg}")
        childRef ! msg
        Thread.sleep(100)
    }
}

object Supervision extends App {
  val system = ActorSystem("supervision")

  val hera = system.actorOf(Props[Hera], "hera")

  hera ! "Resume"
  //hera ! "Restart"
  //hera ! "Stop"
  Thread.sleep(1000)
  println()

  println("system terminate...")
  system.terminate()
}
