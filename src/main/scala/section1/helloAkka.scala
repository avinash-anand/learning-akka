package section1

import akka.actor.{Actor, ActorSystem, Props}

case class GreetMessage(who: String)

class GreeterActor extends Actor {
  override def receive: Receive = {
    case GreetMessage(who) => println(s"Hello $who")
  }
}

object HelloAkka extends App {

  val system = ActorSystem("hello-world")
  val greeter = system.actorOf(props = Props[GreeterActor], "greetor-actor")
  greeter ! GreetMessage("Akka")
  system.terminate()

}
