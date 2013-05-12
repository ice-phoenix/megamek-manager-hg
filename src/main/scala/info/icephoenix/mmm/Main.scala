package info.icephoenix.mmm

import akka.actor._
import info.icephoenix.mmm.data._
import org.apache.log4j.BasicConfigurator

object Main {

  def main(args: Array[String]) {

    BasicConfigurator.configure()

    val mmm = MegamekManager.create()

    mmm.RunnerSup ! StartServer(2345, "")
    mmm.RunnerSup ! StartServer(2346, "")
    mmm.RunnerSup ! StartServer(2347, "")
    mmm.RunnerSup ! StartServer(2348, "")

  }

}
