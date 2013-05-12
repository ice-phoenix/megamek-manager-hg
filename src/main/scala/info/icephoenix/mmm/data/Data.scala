package info.icephoenix.mmm.data

sealed trait Message;

///////////////////////////////////////////////////////////////////////////////
// Server status messages
///////////////////////////////////////////////////////////////////////////////

case class ServerReport(port: Int) extends Message;

sealed trait ServerStatus extends Message;

case class ServerOnline(port: Int, players: Seq[String]) extends ServerStatus;

case class ServerStopped(port: Int) extends ServerStatus;

case class ServerTimedOut(port: Int) extends ServerStatus;

case class ServerFailed(port: Int, error: String) extends ServerStatus;


case object AllServerReport extends Message;

case class AllServerStatus(status: Seq[ServerStatus]) extends Message;

///////////////////////////////////////////////////////////////////////////////
// Server start/stop messages
///////////////////////////////////////////////////////////////////////////////

case class StartServer(port: Int, password: String) extends Message;

case class StopServer(port: Int) extends Message;
