package com.vogon101.hexacomb

import java.net.Socket

/**
 * Created by Freddie Poser on 10/08/2015.
 *
 */
class CustomConnectionManager(val _c: Socket, val _s: Server, val _OM: OutputManager) extends ConnectionManager(_c, _s, _OM){

  override def run (): Unit = {
    out.println(server.welcome)
    conn.close()
  }

}
