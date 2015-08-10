package com.vogon101.hexacomb.examples

import java.net.Socket

import com.vogon101.hexacomb.{ ConnectionManager, OutputManager, Server }

/**
 * Simple example of a ConnectionManager
 * @param _c - The socket instance of the connection
 * @param _s - The server instance
 * @param _OM - The instance of OutputManager
 */
class CustomConnectionManager(val _c: Socket, val _s: Server, val _OM: OutputManager) extends ConnectionManager(_c, _s, _OM){

  override def run (): Unit = {
    out.println(server.welcome)
    conn.close()
  }

}
