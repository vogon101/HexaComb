package com.vogon101.hexacomb

import scala.io.Source

/**
 * Created by Freddie Poser on 10/08/2015.
 */
class DefaultConsoleInteractionManager(server: Server) extends ServerInteractionManager{

  def run(): Unit = {
    while (server.running) {
      server.command(readLine())
    }
  }

}
