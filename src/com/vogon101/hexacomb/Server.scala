package com.vogon101.hexacomb

import java.io.PrintStream
import java.net._
import scala.util.matching.Regex


/**
 * Main class that manages the server
 * @param port - The port for the server to run on
 * @param address - The hostname or ip address of the server
 * @param _name - The name of the server
 * @param _welcome - The welcome message of the server
 * @param _maxConnections - The maximum number of connections a server can handle
 * @param OM - The instance of the output manager
 * @param ConnectionManagerBuilder - The function that retruns a {@see ConnectionManager} for every request
 */
class Server (
  val OM: OutputManager,
  val port: Int = 4242,
  val address: String = "127.0.0.1",
  private var _name: String = "Server",
  private var _welcome: String = "Hi :D",
  private var _maxConnections: Int = 100

)(ConnectionManagerBuilder : (Socket, Server) => ConnectionManager)
  extends Runnable{

  OM.notice(s"Server instance initiated [PORT: $port | ADDR: $address | NAME: $name]")


  private val socket = new ServerSocket(port, 10, InetAddress.getByName(address))

  /**
   * The current list of connection managers
   */
  var connections: List[ConnectionManager] = List()
  /**
   * The current list of threads
   */
  var threads: List[Thread] = List()
  /**
   * Total number of successful connections since start
   */
  var totalConnections = 0
  /**
   * Total number of rejected connections
   */
  var rejectedConnections = 0

  private var inputManager: ServerInteractionManager = null

  private var _running: Boolean = true

  OM.info("Server address [" + socket.getInetAddress.getHostName+":"+port+"]")

  /**
   * Start the server.
   * Only called once
   */
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

  /**
   * Stop the server.
   * Ends all connections, then closes the socket, then stops the actual server
   * @param kill - If true the server will force stop the threads that run the ConnectionManagers
   */
  def stop(kill: Boolean): Unit = {
    OM.notice("Stopping server")
    connections.foreach(_.forceStop(kill))
    _running = false
    socket.close()
    if (kill)
      Thread.currentThread().stop()
    OM.notice("Server stopped, goodbye")
  }

  /**
   * Run a command in the server
   * This can be called by any part of the application but is most-likely called by a ServerInteractionManager
   * {@todo This is not fully implemented}
   * @param command - The command to be executed
   * The current supported commands are:
   *    info - display server info
   *    serverVars - display the current server variables
   *    fullInfo - info followed by serverVars
   *    setMaxConnections [maxConnections]
   *    setName [name]
   *    setWelcomeMessage [WelcomeMessage]
   *    setUse[Color/Colour] [true/false]
   */
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

  protected def runGenCommand (c: String, a: String): Unit = {
    val colorMatch = """setUseColou?r""".r
    c match {
      case colorMatch() => OM.color = a.toBoolean; OM.notice("Use colors set to " + OM.color)
      case _ => OM.error("Command not found")
    }
  }

  /**
   * Set the ServerInteractionManager that this instance uses
   * @param im - The new instance of ServerInteractionManager
   */
  def setInputManager (im: ServerInteractionManager) = inputManager = im

  /**
   * Is the server running?
   * @return
   */
  def running = _running

  /**
   *  Manages a connection
   *  Starts a new ConnectionManager by  running the ConnectionManagerBuilder function passed in construction,
   *  then starts a new thread with that ConnectionManager
   * @param connection - The socket instance of the connection
   */
  protected def manageConnection (connection: Socket): Unit = {
    val manager = ConnectionManagerBuilder( connection, this )
    connections = manager :: connections
    totalConnections += 1
    val t = new Thread( manager )
    t.start( )
  }

  /**
   * print the server vars to the console ie name, maxConnections ...
   */
  def printVars(): Unit =  {
    OM.notice("=====Server Vars=====")
    OM.info(s"Name            : $name")
    OM.info(s"Max Connections : $maxConnections")
    OM.info(s"Welcome Message : $welcome")
    OM.info(s"Use Colors      : "+OM.color)
    OM.info(s"Full Address    : "+socket.getInetAddress.getHostAddress+" : "+port)
    OM.notice("=====================")
  }

  /**
   * The maximum connections this server will handle
   * @return
   */
  def maxConnections = _maxConnections

  /**
   * The freindly name of this server
   * @return
   */
  def name = _name

  /**
   * The welcome message of this server
   * @return
   */
  def welcome = _welcome

  /**
   * Remove a connection from this server's list [Does not remove the thread]
   * @param manager - The connection manager to be removed
   */
  def removeConnection (manager: ConnectionManager) = connections = connections.filterNot(elm => elm == manager)

  /**
   * Print the server info to the console [totalConnections, currentConnections, rejectedConnections]
   */
  def printInfo () = OM.info("Successful connections: " + totalConnections + " | Current connections: " + connections.length + " | Rejected connections: " + rejectedConnections)

  /**
   * Start the server, alias for start to implement Runnable
   */
  def run () = start()

}

object Server {

  /**
   * Default builder for a standard ConnectionManager
   * @param socket - The socket instance of the connection
   * @param server - The server instance
   * @return Instance of ConnectionManager
   */
  def DefaultConnectionManagerBuilder (socket: Socket, server: Server) = new ConnectionManager(socket, server, server.OM)

}
