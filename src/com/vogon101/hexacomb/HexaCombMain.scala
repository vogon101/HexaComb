package com.vogon101.hexacomb

import java.net.Socket

/**
 * Created by Freddie Poser on 10/08/2015.
 *
 */
object HexaCombMain extends App{

  HexaCombSimple.simpleStart(args)(Server.DefaultConnectionManagerBuilder)

}

object HexaCombSimple {

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

    val OM = new OutputManager(USE_COLORS)

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