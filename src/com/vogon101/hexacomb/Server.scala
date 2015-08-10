package com.vogon101.hexacomb

import java.io.PrintStream
import java.net._
import scala.util.matching.Regex


/**
 * Created by Freddie Poser on 10/08/2015.
 *
 */
class Server ( val port: Int,
  val address: String = "127.0.0.1",
  private var _name: String = "Server",
  private var _welcome: String = "Hi :D",
  private var _maxConnections: Int = 10,
  val OM: OutputManager
)(ConnectionManagerBuilder : (Socket, Server) => ConnectionManager)
  extends Runnable{

  OM.notice(s"Server instance initiated [PORT: $port | ADDR: $address | NAME: $name]")


  private val socket = new ServerSocket(port, 10, InetAddress.getByName(address))

  var connections: List[ConnectionManager] = List()
  var threads: List[Thread] = List()
  var totalConnections = 0
  var rejectedConnections = 0

  private var inputManager: ServerInteractionManager = null

  private var _running: Boolean = true

  OM.info("Server address [" + socket.getInetAddress.getHostName+":"+port+"]")

  def start (): Unit = {
    OM.notice ("Server now accepting connections")
    println()
    while (running) {
      var connection: Socket = null
      try {
        connection = socket.accept( )
      }
      catch{
        case se: SocketException => {
          OM.warn("An exception was encountered when trying to accept a connection")
          OM.notice("Looks like the socket was closed, Connections are no longer accepted")
          return
        }
      }

      if (connections.length == maxConnections) {
        val out: PrintStream  = new PrintStream(connection.getOutputStream)
        out.println("The server is full, please try again")
        connection.close()
        OM.warn("A connection was attempted but the server is running at capacity so it had to reject it")
        rejectedConnections += 1
      }
      else {
        manageConnection(connection)
      }
      printInfo()
    }
  }

  def stop(kill: Boolean): Unit = {
    OM.notice("Stopping server")
    connections.foreach(_.forceStop(kill))
    socket.close()
    _running = false
    if (kill)
      Thread.currentThread().stop()
    OM.notice("Server stopped, goodbye")
  }

  def command (command: String): Unit = {
    if (command == "stop") {
      stop(kill = true)
    }
    else if (command == "info") {
      printInfo()
    }
    else if (command == "serverVars") {
      printVars()
    }
    else if (command ==  "fullInfo") {
      printInfo()
      printVars()
    }
    else {

      val spaceSplitter = """ (?=([^\"]*\"[^\"]*\")*[^\"]*$)"""

      command.split(spaceSplitter).sliding(2, 1).toList.collect {
        case Array("setMaxConnections", maxCons: String) => _maxConnections = maxCons.toInt; OM.notice("Max connections set to " + maxConnections)
        case Array("setName", name: String) => _name = name.replaceAll("\"", ""); OM.notice("Server name set to " + name)
        case Array("setWelcomeMessage", welcome: String) => _welcome = welcome.replaceAll("\"", ""); OM.notice("Server welcome message set to " + welcome)
        case Array(genCommand: String, argument: String) => runGenCommand(genCommand, argument)
        case _ => OM.error("Command not found or bad arguments format")
      }
    }
  }

  def runGenCommand (c: String, a: String): Unit = {
    val colorMatch = """setUseColou?r""".r
    c match {
      case colorMatch() => OM.color = a.toBoolean; OM.notice("Use colors set to " + OM.color)
      case _ => OM.error("Command not found")
    }
  }

  def setInputManager (im: ServerInteractionManager) = inputManager = im

  def running = _running

  def manageConnection (connection: Socket): Unit = {
    val manager = ConnectionManagerBuilder( connection, this )
    connections = manager :: connections
    totalConnections += 1
    val t = new Thread( manager )
    t.start( )
  }

  def printVars(): Unit =  {
    OM.notice("=====Server Vars=====")
    OM.info(s"Name            : $name")
    OM.info(s"Max Connections : $maxConnections")
    OM.info(s"Welcome Message : $welcome")
    OM.info(s"Use Colors      : "+OM.color)
    OM.info(s"Full Address    : "+socket.getInetAddress.getHostAddress+" : "+port)
    OM.notice("=====================")
  }

  def maxConnections = _maxConnections
  def name = _name
  def welcome = _welcome
  def removeConnection (manager: ConnectionManager) = connections = connections.filterNot(elm => elm == manager)
  def printInfo () = OM.info("Successful connections: " + totalConnections + " | Current connections: " + connections.length + " | Rejected connections: " + rejectedConnections)
  def run () = start()

}

object Server {

  def DefaultConnectionManagerBuilder (socket: Socket, server: Server) = new ConnectionManager(socket, server, server.OM)

}
