# HexaComb
Simple scala, raw server utility. This can be accessed through putty by selecting the "raw" setting. It can also be used for http or any other protocol by providing a custom ConnectionManager

##Usage
###Basic
To quickly get started with HexaComb use the HexaCombSimple utility. 
```scala
import com.vogon101.hexacomb.HexaCombSimple
object Test extends App{

  HexaCombSimple.simpleStart(args)(Server.DefaultConnectionManagerBuilder)

}
```
This creates a basic echo server and reads in the commandline arguments to provide the settings. Bellow are the arguments, they are optional and the defaults are listed in square brackets:

```
  --ip <IP / Hostname> []
  --port <Port to bind to> [4242] 
  --name <Server name> [Default HoneyComb Server]
  --color <true/false (use color in output)> [false]
  --welcome <Server welcome message> [Welcome to <SERVER NAME>]
```
###Customisable
The main way of customizing HexaComb is by providing a `ConnectionManager`. This is where individual connections are managed. To create one simply extend the base class `ConnectionManager` and override the `run()` method. Then, when you call `HexaCombSimple.simpleStart(args)(BuilderFunction)` pass it an anonymous function that creates the new `CustomConnectionManager`. This function will be called with every new request to provide the connection handler which will then be run in a new thread to  manage the connection. You can put logic in here to provide a different `ConnectionHandler` for each different type of request

```scala
object HexaCombMain extends App{

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
    //Pass it the socket and the instance of the Server
    //HexaCombsimple provides an OutputManager
    new CustomConnectionManager(socket,server, HexaCombSimple.OM)
  })

}
```

