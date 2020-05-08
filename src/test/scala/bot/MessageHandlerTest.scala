package bot

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class MessageHandlerTest extends AnyFlatSpec with Matchers {
  val messageHandler = new MessageHandler()

  "Person without messages" should "have empty list of messages" in {
    assert(messageHandler.show("1") == "")
  }

  "Clear" should "clear" in {
    messageHandler.send("2", "1", "mem")
    messageHandler.clear("1")
    assert(messageHandler.show("1") == "")
  }

  "Person with messages" should "have non-empty list of messages" in {
    messageHandler.send("2", "1", "mem")
    assert(messageHandler.show("1") == "From: 2\nmem\n\n")
    messageHandler.clear("1")
  }

  "Messages from other people" should "be different" in {
    messageHandler.send("2", "1", "kek")
    messageHandler.send("3", "1", "kek")
    print(messageHandler.show("1"))
    assert(messageHandler.show("1") == "From: 2\nkek\n\nFrom: 3\nkek\n\n")
    messageHandler.clear("1")
  }

}