package com.vogon101.hexacomb

import java.net.Socket

/**
 * Created by Freddie Poser on 10/08/2015.
 *
 */
object HexaCombMain extends App{

  //Create an instance of the OutputManager, this is the first thing to be done and is universal
  val OM = new OutputManager(true)

  //Define a custom connection manager
  class CustomConnectionManager(val _c: Socket, val _s: Server, val _OM: OutputManager) extends ConnectionManager(_c, _s, _OM){
    //Override the run method so handle the request
    override def run (): Unit = {
      //reply with the welcome message, pass it the server name for use (i.e. Welcome to %s => NAME)s
      out.println(server.welcome.format(server.name))
      conn.close()
    }
  }

  //Now start the server using simple start but pass it your own builder to create the managers for each request
  //At every request the builder function is called to provide the right connection manager
  //This can be used to provide a different manager for different types of request
  HexaCombSimple.simpleStart(args) ((socket: Socket, server: Server) => {
    new CustomConnectionManager(socket,server, HexaCombSimple.OM)
  })

}

object HexaCombSimple {

  val OM = new OutputManager(false)

  def simpleStart (args: Array[String]) (ConnectionManagerBuilder : (Socket, Server) => ConnectionManager): Unit = {
    var SERVER_NAME = "Default HoneyComb Server"
    var SERVER_PORT = 4242
    var SERVER_IP = ""
    var USE_COLORS = false
    var SERVER_WELCOME = "Welocme to %s"

    args.sliding(2, 1).toList.collect {
      case Array("--ip", argIP: String) => SERVER_IP = argIP
      case Array("--port", argPort: String) => SERVER_PORT = argPort.toInt
      case Array("--name", argName: String) => SERVER_NAME = argName
      case Array("--color", argColor: String) => USE_COLORS = argColor.toBoolean
      case Array("--welcome", argWelcome: String) => SERVER_WELCOME = argWelcome
    }

    OM .color = USE_COLORS

    val server = new Server(SERVER_PORT, SERVER_IP, SERVER_NAME, SERVER_WELCOME, _maxConnections = 100, OM = OM) (ConnectionManagerBuilder)
    val ServerThread = new Thread(server)
    ServerThread.start()

    val serverInputManager = new DefaultConsoleInteractionManager(server)
    new Thread(serverInputManager).start()
  }
  
}

/*
var SERVER_NAME = "Default HoneyComb Server"
  var SERVER_PORT = 4242
  var SERVER_IP = ""
  var USE_COLORS = false
  var SERVER_WELCOME = "Welocme to %s"

  args.sliding(2, 1).toList.collect {
    case Array("--ip", argIP: String) => SERVER_IP = argIP
    case Array("--port", argPort: String) => SERVER_PORT = argPort.toInt
    case Array("--name", argName: String) => SERVER_NAME = argName
    case Array("--color", argColor: String) => USE_COLORS = argColor.toBoolean
    case Array("--welcome", argWelcome: String) => SERVER_WELCOME = argWelcome
  }

  val OM = new OutputManager(USE_COLORS)

  val server = new Server(SERVER_PORT, SERVER_IP, SERVER_NAME, SERVER_WELCOME, _maxConnections = 100, OM = OM) (Server.DefaultConnectionManagerBuilder)
  val ServerThread = new Thread(server)
  ServerThread.start()

  val serverInputManager = new DefaultConsoleInteractionManager(server)
  new Thread(serverInputManager).start()

 */