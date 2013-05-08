package info.icephoenix.mmm.msgs

sealed trait Message;

case class Result(success: Boolean, msg: String) extends Message;

object Success {def apply(msg: String) = Result(true, msg)}

object Failure {def apply(msg: String) = Result(false, msg)}

case object ServerStatsRequest extends Message;

case class ServerStatsResponse(port: Int, players: Iterable[String]) extends Message;

case object AllServersStatsRequest extends Message;

case class AllServersStatsResponse(stats: Iterable[ServerStatsResponse]) extends Message;

case class StartServer(port: Int, password: String) extends Message;

case class StopServer(port: Int) extends Message;
