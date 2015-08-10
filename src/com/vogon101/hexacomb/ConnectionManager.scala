package com.vogon101.hexacomb

import java.io.PrintStream
import java.net.{ SocketException, Socket }

import scala.io.BufferedSource

/**
 * Base class for a ConnectionManager, if instantiated it will act as an echo server
 * @param conn - The Socket instance of the connection
 * @param server - The server instance
 * @param OM - The instance of the OutputManager
 */
class ConnectionManager(
  protected val conn: Socket,
  protected val server: Server,
  protected val OM: OutputManager
) extends Runnable{

  //TODO: Custom BufferedSource for readline

  /**
   * The input stream for reading the user input
   */
  val in : BufferedSource = new BufferedSource(conn.getInputStream)
  /**
   * The output stream for writing to the user
   */
  val out: PrintStream    = new PrintStream(conn.getOutputStream)

  private var thread: Thread = null

  private var running = true

  /**
   * Run the connection response, will end when the connection is closed
   */
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

  /**
   * Stop the connection
   * @param kill - Also force the thread to close
   */
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
