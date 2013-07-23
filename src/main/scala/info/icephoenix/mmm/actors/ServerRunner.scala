package info.icephoenix.mmm.actors

import akka.actor.{ActorRef, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import info.icephoenix.mmm.data._
import megamek.common.Player
import megamek.common.event._
import megamek.server.Server
import org.joda.time.{DateTime, Period}
import scala.collection.JavaConversions._

class ServerRunner(port: Int, password: String = "")
  extends Actor
  with Logging {

  var mms: Server = _

  var cTimeFrom = new DateTime()
  var mTimeFrom = cTimeFrom

  override def preStart() {
    mms = new Server(password, port)

    def updateMTime() { mTimeFrom = new DateTime() }

    mms.getGame.addGameListener(new GameListener {
      def gameEntityChange(e: GameEntityChangeEvent) { updateMTime() }

      def gamePlayerChat(e: GamePlayerChatEvent) { updateMTime() }

      def gameEntityRemove(e: GameEntityRemoveEvent) { updateMTime() }

      def gamePlayerConnected(e: GamePlayerConnectedEvent) { updateMTime() }

      def gameBoardNew(e: GameBoardNewEvent) { updateMTime() }

      def gameTurnChange(e: GameTurnChangeEvent) { updateMTime() }

      def gamePlayerChange(e: GamePlayerChangeEvent) { updateMTime() }

      def gamePhaseChange(e: GamePhaseChangeEvent) { updateMTime() }

      def gameEnd(e: GameEndEvent) { updateMTime() }

      def gameNewAction(e: GameNewActionEvent) { updateMTime() }

      def gameEntityNewOffboard(e: GameEntityNewOffboardEvent) { updateMTime() }

      def gameEntityNew(e: GameEntityNewEvent) { updateMTime() }

      def gameReport(e: GameReportEvent) { updateMTime() }

      def gameBoardChanged(e: GameBoardChangeEvent) { updateMTime() }

      def gamePlayerDisconnected(e: GamePlayerDisconnectedEvent) { updateMTime() }

      def gameSettingsChange(e: GameSettingsChangeEvent) { updateMTime() }

      def gameMapQuery(e: GameMapQueryEvent) { updateMTime() }
    })
  }

  override def postStop() {
    mms.die()
  }

  def sendStatus(sender: ActorRef) {

    def playerToString(p: Player) = {
      val suffix = (p.isGhost, p.isObserver) match {
        case (true, _) => "[ghost]"
        case (_, true) => "[obs]"
        case _ => ""
      }
      p.getName + suffix
    }

    val now = new DateTime()

    sender ! ServerOnline(
      port,
      Option(mms.getGame)
      .map { _.getPlayers.map { playerToString }.toList }
      .getOrElse { List.empty[String] },
      new Period(cTimeFrom, now),
      new Period(mTimeFrom, now)
    )
  }

  def receive = {

    case ServerReport(`port`) => {
      sendStatus(sender)
    }

    case ResetServer(`port`) => {
      mms.resetGame()
      sendStatus(sender)
    }

    case msg: Message => {
      logger.debug("Unknown msg: {}", msg)
    }

  }
}
