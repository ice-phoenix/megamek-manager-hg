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

  def queryServerStatus(child: ActorRef, port: Int) = {
    ask(child, ServerReport(port)).mapTo[ServerOnline] recover {
      case t: TimeoutException => ServerTimedOut(port)
      case e: Exception => ServerFailed(port, e.getMessage)
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

          sender ! Await.result(queryServerStatus(runner, port), Duration.Inf)

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
        case ref => {
          logger.debug("Stopping server on port {}", port.toString)

          context.stop(ref)

          sender ! ServerStopped(port)

        }
      }
    }

    case Terminated(child) => {
      logger.debug("Terminated {}", child)
      runners.inverse().remove(child)
    }

    case ServerReport(port) => {
      runners.get(port) match {
        case null => {
          sender ! FailWith.ServerNotRunningOn(port)
        }
        case ref => {
          logger.debug("Querying server stats on port {}", port.toString)

          sender ! Await.result(queryServerStatus(ref, port), Duration.Inf)

        }
      }
    }

    case AllServerReport => {
      logger.debug("Querying all server stats")

      val fAllStats = Future.traverse(context.children) {
        child => queryServerStatus(child, runnerPort(child))
      }
      val allStats = Await.result(fAllStats, Duration.Inf).toSeq

      sender ! AllServerStatus(allStats)

    }

    case msg: Message => {
      logger.debug("Unknown msg: {}", msg)
    }

  }

}
