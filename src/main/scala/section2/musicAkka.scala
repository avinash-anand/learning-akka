package section2

import akka.actor.{Actor, ActorSystem, Props}
import section2.MusicController.{Play, Stop}
import section2.MusicPlayer.{MusicStart, MusicStop}

class MusicController extends Actor {
  override def receive: Receive = {
    case Play => println("music started")
    case Stop => println("music stopped")
  }
}
//1. standard practice : define props inside companion object, never define props inside another actor
// for eg: inside MusicPlayer, we shouldn't do context.actorOf(Props[MusicController], "controller"). It's bad.
// 2. standard practice : define messages of an actor usually inside companion object, usually by extending sealed trait
// That way pattern matching is strong and also maintainable code
object MusicController {
  sealed trait ControllerMsg
  case object Play extends ControllerMsg
  case object Stop extends ControllerMsg
  def props: Props = Props[MusicController]
}

class MusicPlayer extends Actor {
  override def receive: Receive = {
    case MusicStop => println("I don't want to stop music!")
    case MusicStart =>
      val controller = context.actorOf(MusicController.props, "controller")
      controller ! Play
    case _ => println("Unknown message")
  }
}

object MusicPlayer {
  sealed trait PlayMsg
  case object MusicStop extends PlayMsg
  case object MusicStart extends PlayMsg
}

object Creation extends App {

  val system = ActorSystem("creation")
  val player = system.actorOf(Props[MusicPlayer], "player")
  player ! MusicStart
  player ! MusicStop
  system.terminate()
}