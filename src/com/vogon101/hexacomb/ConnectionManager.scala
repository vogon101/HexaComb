package com.vogon101.hexacomb

import java.io.PrintStream
import java.net.{ SocketException, Socket }

import scala.io.BufferedSource

/**
 * Created by Freddie Poser on 10/08/2015.
 *
 */
class ConnectionManager(conn: Socket, server: Server, val OM: OutputManager) extends Runnable{

  val in : BufferedSource = new BufferedSource(conn.getInputStream)
  val out: PrintStream    = new PrintStream(conn.getOutputStream)

  private var thread: Thread = null

  private var running = true

  def run (): Unit = {
    OM.success("Connected to " + conn.getRemoteSocketAddress)
    thread = Thread.currentThread()
    try {
      out.println(server.welcome.format(server.name))
      while( running && in.hasNext ) {
        out.println( in.getLines( ).next( ) )
      }
    }
    catch {
      case se : SocketException => OM.warn("An exception was encountered when reading data from " + conn.getRemoteSocketAddress)
    }
    server.removeConnection(this)
    if (running)
      forceStop()
    OM.success("Connection closed")
    server.printInfo()
  }

  def forceStop(kill: Boolean = false) ={
    running=false
    conn.shutdownInput()
    conn.shutdownOutput()
    conn.close()
    if (kill)
      thread.stop()
    OM.notice("ConnectionManager closed")
  }

}
