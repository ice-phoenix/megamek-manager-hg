package info.icephoenix.mmm.sups

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import com.google.common.collect.HashBiMap
import com.typesafe.scalalogging.slf4j.Logging
import info.icephoenix.mmm.actors.ServerRunner
import info.icephoenix.mmm.msgs._
import scala.concurrent.duration._

class RunnerSupervisor
  extends Actor
  with Logging {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 6, withinTimeRange = 1 minute) {
      case _: Exception => Stop
    }

  def serverRunnerName(port: Int) = { s"runner-$port" }

  val runners = HashBiMap.create[Int, ActorRef]()

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
        }
        case _ => {}
      }
    }

    case StopServer(port) => {
      runners.get(port) match {
        case null => {}
        case ref => {
          logger.debug("Stopping server on port {}", port.toString)
          context.stop(ref)
        }
      }
    }

    case Terminated(child) => {
      logger.debug("Terminated {}", child)
      runners.inverse().remove(child)
    }

  }

}
