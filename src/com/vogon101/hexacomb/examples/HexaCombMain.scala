package com.vogon101.hexacomb.examples

import java.net.Socket

import com.vogon101.hexacomb.{ HexaCombSimple, Server }

/**
 * Created by Freddie Poser on 10/08/2015.
 *
 */
object HexaCombMain extends App{
  //Start the server using simple start but pass it your own builder to create the managers for each request
  //At every request the builder function is called to provide the right connection manager
  //This can be used to provide a different manager for different types of request
  HexaCombSimple.simpleStart(args) ((socket: Socket, server: Server) => {
    new CustomConnectionManager(socket,server, HexaCombSimple.OM)
  })

}