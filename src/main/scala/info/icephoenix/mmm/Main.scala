package info.icephoenix.mmm

import akka.actor._
import info.icephoenix.mmm.msgs._
import info.icephoenix.mmm.sups.RunnerSupervisor
import org.apache.log4j.BasicConfigurator

object Main {

  def main(args: Array[String]) {

    BasicConfigurator.configure()

    val system = ActorSystem("megamek-manager")

    val runnerSup = system.actorOf(
      Props[RunnerSupervisor],
      name = "runner-sup"
    )

    runnerSup ! StartServer(2345, "")

  }

}
