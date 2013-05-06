package info.icephoenix.mmm.msgs

sealed trait Message;

case object StatisticsRequest extends Message;

case class StatisticsResponse(port: Int, players: Iterable[String]) extends Message;

case class StartServer(port: Int, password: String) extends Message;

case class StopServer(port: Int) extends Message;
