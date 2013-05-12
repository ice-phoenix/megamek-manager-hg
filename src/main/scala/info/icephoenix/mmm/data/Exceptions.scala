package info.icephoenix.mmm.data

import akka.actor.Status.Failure

case class ServerAlreadyRunningOn(port: Int) extends Exception(s"Server on port $port is already running");

case class ServerNotRunningOn(port: Int) extends Exception(s"No server on port $port");

case object FailWith {

  def ServerAlreadyRunningOn(port: Int) = Failure(new ServerAlreadyRunningOn(port))

  def ServerNotRunningOn(port: Int) = Failure(new ServerNotRunningOn(port))

}
