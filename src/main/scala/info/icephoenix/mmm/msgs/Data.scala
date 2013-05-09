package info.icephoenix.mmm.msgs

sealed trait Data;

sealed trait ServerStatus extends Data;

case class ServerOnline(port: Int, players: Seq[String]) extends ServerStatus;

case class ServerTimedOut(port: Int) extends ServerStatus;

case class ServerFailed(port: Int, error: String) extends ServerStatus;

object DataImplicits {
  implicit def ServerStatsResponse2ServerOnline(v: ServerStatsResponse) = {
    (ServerOnline.apply _).tupled(ServerStatsResponse.unapply(v).get)
  }

  implicit def ServerStatsResponse2ServerStatus(v: ServerStatsResponse) = {
    ServerStatsResponse2ServerOnline(v).asInstanceOf[ServerStatus]
  }
}
