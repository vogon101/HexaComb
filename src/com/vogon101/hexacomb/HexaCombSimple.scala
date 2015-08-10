package com.vogon101.hexacomb

import java.net.Socket

/**
 * Created by Freddie Poser on 10/08/2015.
 *
 */
object HexaCombSimple {

  /**
   * Ready made instance of OutputManager with USE_COLORS = false
   */
  val OM = new OutputManager(false)

  /**
   * Simple start for a hexacomb server. Uses the standard instance of Server and gets the settings from a array of console arguments
   * @param args - The array console arguments
   * @param ConnectionManagerBuilder - The function that provides the ConnectionManagers for each request
   */
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

    val server = new Server(OM, SERVER_PORT, SERVER_IP, SERVER_NAME, SERVER_WELCOME, _maxConnections = 100) (ConnectionManagerBuilder)
    val ServerThread = new Thread(server)
    ServerThread.start()

    val serverInputManager = new DefaultConsoleInteractionManager(server)
    new Thread(serverInputManager).start()
  }
}