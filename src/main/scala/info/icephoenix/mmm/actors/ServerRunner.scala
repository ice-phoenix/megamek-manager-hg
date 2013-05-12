package info.icephoenix.mmm.actors

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import info.icephoenix.mmm.data._
import megamek.common.Player
import megamek.server.Server
import scala.collection.JavaConversions._

class ServerRunner(port: Int, password: String = "")
  extends Actor
  with Logging {

  var mms: Server = _

  override def preStart() {
    mms = new Server(password, port)
  }

  override def postStop() {
    mms.die()
  }

  def receive = {

    case ServerReport(`port`) => {

      def playerToString(p: Player) = {
        val suffix = (p.isGhost, p.isObserver) match {
          case (true, _) => "[ghost]"
          case (_, true) => "[obs]"
          case _ => ""
        }
        p.getName + suffix
      }

      sender ! ServerOnline(
        port,
        Option(mms.getGame)
          .map { _.getPlayers.map { playerToString }.toList }
          .getOrElse(List.empty[String])
      )
    }

    case msg: Message => {
      logger.debug("Unknown msg: {}", msg)
    }

  }
}
