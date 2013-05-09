package info.icephoenix.mmm.sups

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import com.google.common.collect.HashBiMap
import com.typesafe.scalalogging.slf4j.Logging
import info.icephoenix.mmm.actors.ServerRunner
import info.icephoenix.mmm.msgs.DataImplicits._
import info.icephoenix.mmm.msgs._
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

  def serverRunnerName(port: Int) = { s"runner-$port" }

  def serverRunnerPort(child: ActorRef) = runners.inverse().get(child)

  def receive = {

    case StartServer(port, password) => {
      runners.get(port) match {
        case null => {
          logger.debug("Starting server on port {}", port.toString)
          val runner = context.actorOf(
            Props(new ServerRunner(port, password)),
            name = serverRunnerName(port)
          )
          runners.put(port, runner)
          context.watch(runner)

          sender ! Success(s"Server on port $port started")

        }
        case _ => {
          sender ! Failure(s"Server on port $port is already started")
        }
      }
    }

    case StopServer(port) => {
      runners.get(port) match {
        case null => {
          sender ! Failure(s"Server on port $port is already stopped")
        }
        case ref => {
          logger.debug("Stopping server on port {}", port.toString)
          context.stop(ref)

          sender ! Success(s"Server on port $port stopped")

        }
      }
    }

    case Terminated(child) => {
      logger.debug("Terminated {}", child)
      runners.inverse().remove(child)
    }

    case AllServersStatsRequest => {

      val fAllStats = Future.traverse(context.children) {
        child => ask(child, ServerStatsRequest).mapTo[ServerStatsResponse].map { ServerStatsResponse2ServerStatus } recover {
          case t: TimeoutException => ServerTimedOut(serverRunnerPort(child))
          case e: Exception => ServerFailed(serverRunnerPort(child), e.getMessage)
        }
      }

      val allStats = Await.result(fAllStats, Duration.Inf).toSeq

      sender ! AllServersStatsResponse(allStats)

    }

  }

}
