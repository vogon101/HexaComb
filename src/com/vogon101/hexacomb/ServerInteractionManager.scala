package com.vogon101.hexacomb

/**
 * Manages interaction with the server in another thread
 */
abstract class ServerInteractionManager extends Runnable {

}

//TODO: MAKE STATIC
class OutputManager (var color: Boolean){

  val WARN    = "[WARN   ] "
  val NOTICE  = "[NOTICE ] "
  val INFO    = "[INFO   ] "
  val SUCCESS = "[SUCCESS] "
  val ERROR   = "[ERROR  ] "

  def warn (message: String): Unit = {
    if (color)
      println(Console.YELLOW + WARN + message + Console.RESET)
    else
      println(WARN + message)
  }

  def notice (message: String): Unit = {
    if (color)
      println(Console.BLUE + NOTICE + message + Console.RESET)
    else
      println(NOTICE + message)
  }

  def info (message: String): Unit = {
    if (color)
      println(Console.CYAN + INFO + message + Console.RESET)
    else
      println(INFO + message)
  }

  def success (message: String): Unit = {
    if( color )
      println( Console.GREEN + SUCCESS + message + Console.RESET )
    else
      println( SUCCESS + message )
  }
  def error (message: String): Unit = {
      if (color)
        println(Console.RED + ERROR + message + Console.RESET)
      else
        println(ERROR + message)

    }

}
