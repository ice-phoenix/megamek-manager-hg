package info.icephoenix.mmm.sups

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import com.google.common.collect.HashBiMap
import com.typesafe.scalalogging.slf4j.Logging
import info.icephoenix.mmm.actors.ServerRunner
import info.icephoenix.mmm.data._
import scala.concurrent._
import scala.concurrent.duration._

class RunnerSupervisor
  extends Actor
  with Logging {

  val runners = HashBiMap.create[Int, ActorRef]()

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 6, withinTimeRange = 1 minute) {
      case _: Exception => Stop
    }

  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  def runnerName(port: Int) = { s"runner-$port" }

  def runnerPort(child: ActorRef) = runners.inverse().get(child)

  def forwardMsg(child: ActorRef, msg: ServerMessage) = {
    ask(child, msg).mapTo[ServerOnline] recover {
      case t: TimeoutException => ServerTimedOut(msg.port)
      case e: Exception => ServerFailed(msg.port, e.getMessage)
    }
  }

  def receive = {

    case StartServer(port, password) => {
      runners.get(port) match {
        case null => {
          logger.debug("Starting server on port {}", port.toString)

          val runner = context.actorOf(
            Props(new ServerRunner(port, password)),
            name = runnerName(port)
          )
          runners.put(port, runner)
          context.watch(runner)

          sender ! Await.result(forwardMsg(runner, ServerReport(port)), Duration.Inf)
        }
        case _ => {
          sender ! FailWith.ServerAlreadyRunningOn(port)
        }
      }
    }

    case StopServer(port) => {
      runners.get(port) match {
        case null => {
          sender ! FailWith.ServerNotRunningOn(port)
        }
        case runner => {
          logger.debug("Stopping server on port {}", port.toString)
          context.stop(runner)
          sender ! ServerStopped(port)
        }
      }
    }

    case Terminated(child) => {
      logger.debug("Terminated {}", child)
      runners.inverse().remove(child)
    }

    case msg: ServerMessage => {
      val port = msg.port
      runners.get(port) match {
        case null => {
          sender ! FailWith.ServerNotRunningOn(port)
        }
        case runner => {
          logger.debug("Forwarding msg '{}' to server on port {}", msg, port.toString)
          sender ! Await.result(forwardMsg(runner, msg), Duration.Inf)
        }
      }
    }

    case AllServerReport => {
      logger.debug("Querying all server stats")

      val fAllStats = Future.traverse(context.children) {
        child => forwardMsg(child, ServerReport(runnerPort(child)))
      }
      val allStats = Await.result(fAllStats, Duration.Inf).toSeq

      sender ! AllServerStatus(allStats)
    }

    case msg: Message => {
      logger.debug("Unknown msg: {}", msg)
    }

  }

}
