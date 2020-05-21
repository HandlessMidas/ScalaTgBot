package bot
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.tools.Durations
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MessageHandlerTest extends AnyFlatSpec with Matchers {
  val usersT = TableQuery[Users]
  val messagesT = TableQuery[Messages]
  val messageHandler = new DBMessageHandler(messagesT)
  messageHandler.init()

  "Person without messages" should "have empty list of messages" in {
    val str : String = Await.result(messageHandler.show("1"), Duration.Inf)
    assert(str == "")
  }

  "Clear" should "clear" in {
    messageHandler.send("2", "1", "mem")
    messageHandler.clear("1")
    val str : String = Await.result(messageHandler.show("1"), Duration.Inf)
    assert(str == "")
  }

  "Person with messages" should "have non-empty list of messages" in {
    messageHandler.send("2", "1", "mem")
    val str : String = Await.result(messageHandler.show("1"), Duration.Inf)
    assert(str == "From: 2\nmem\n")
    messageHandler.clear("1")
  }

  "Messages from other people" should "be different" in {
    messageHandler.send("2", "1", "kek")
    messageHandler.send("3", "1", "kek")
    val str : String = Await.result(messageHandler.show("1"), Duration.Inf)
    assert(str == "From: 2\nkek\n\nFrom: 3\nkek\n")
    messageHandler.clear("1")
  }
}