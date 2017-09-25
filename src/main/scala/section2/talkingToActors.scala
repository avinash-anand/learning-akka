package section2

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import section2.Checker.{BlackUser, CheckUser, WhiteUser}
import section2.Recorder.NewUser
import section2.Storage.AddUser

/**
  * imagine registration system. we want to implement the part responsible for adding users in the system.
  * But we need to check before adding a new user to see whether s/he is blacklisted or not.
  * 3 actors - recorder, storage, checker.
  * recorder - handling new user message, send message to checker to check for blacklisting
  * checker - check blacklisting
  * storage - add user message
  */

case class User(username: String, email: String)


object Recorder {
  sealed trait RecorderMsg
  case class NewUser(user: User) extends RecorderMsg
  def props(checker: ActorRef, storage: ActorRef) = Props(new Recorder(checker, storage))
}

object Checker {
  sealed trait CheckerMsg
  case class CheckUser(user: User) extends CheckerMsg
  sealed trait CheckerResponse
  case class BlackUser(user: User) extends CheckerResponse
  case class WhiteUser(user: User) extends CheckerResponse
  def props = Props[Checker]
}

object Storage {
  sealed trait StorageMsg
  case class AddUser(user: User) extends StorageMsg
  def props = Props[Storage]
}

class Storage extends Actor {
  var users = List.empty[User]
  override def receive: Receive = {
    case AddUser(user) =>
      println(s"Storage: $user added")
      user :: users
  }
}

class Checker extends Actor {
  val blacklist = List(
    User("A", "a@a.com")
  )
  override def receive: Receive = {
    case CheckUser(user) if blacklist contains user =>
      println(s"Checker: $user is in the blacklist")
      sender() ! BlackUser(user)
    case CheckUser(user) =>
      println(s"Checker: $user is not in blacklist")
      sender() ! WhiteUser(user)
  }
}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import akka.pattern.ask

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = {
    case NewUser(user) =>
      checker ? CheckUser(user) map {
        case WhiteUser(user) =>
          storage ! AddUser(user)
        case BlackUser(user) =>
          println(s"Recorder: $user is in the blacklist")
      }
  }
}

object TalkToActor extends App {
  val system = ActorSystem("talk-to-actor")
  val checker = system.actorOf(Checker.props, "checker")
  val storage = system.actorOf(Storage.props, "storage")
  val recorder = system.actorOf(Recorder.props(checker, storage), "recorder")
  recorder ! Recorder.NewUser(User("A", "a@a.com"))
  recorder ! Recorder.NewUser(User("B", "b@b.com"))
  Thread.sleep(100)
  system.terminate()
}