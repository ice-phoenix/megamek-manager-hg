package info.icephoenix.mmm.msgs

sealed trait Message;

// General msgs

case class Result(success: Boolean, msg: String) extends Message;

object Success {def apply(msg: String) = Result(true, msg)}

object Failure {def apply(msg: String) = Result(false, msg)}

// Statistics msgs

case object ServerStatsRequest extends Message;

case class ServerStatsResponse(port: Int, players: Seq[String]) extends Message;

case object AllServersStatsRequest extends Message;

case class AllServersStatsResponse(stats: Seq[ServerStatus]) extends Message;

// Start/stop msgs

case class StartServer(port: Int, password: String) extends Message;

case class StopServer(port: Int) extends Message;
