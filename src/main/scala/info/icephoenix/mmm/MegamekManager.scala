package info.icephoenix.mmm

import akka.actor._
import info.icephoenix.mmm.sups.RunnerSupervisor

class MegamekManager {

  val Actors = ActorSystem("megamek-manager")

  val RunnerSup = Actors.actorOf(
    Props[RunnerSupervisor],
    name = "runner-sup"
  )

  def shutdown() {
    Actors.shutdown()
  }

}

object MegamekManager {

  def create() = {
    new MegamekManager()
  }

}
