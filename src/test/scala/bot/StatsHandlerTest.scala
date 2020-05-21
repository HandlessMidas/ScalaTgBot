package bot
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.tools.Durations
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class StatsHandlerTest extends AnyFlatSpec with Matchers {
  val statsT = TableQuery[Stats]
  val statsHandler = new DBStatsHandler(statsT)
  statsHandler.init()

  "Person without links" should "have empty list of cats" in {
    val str : String = Await.result(statsHandler.show("1"), Duration.Inf)
    assert(str == "")
  }

  "Clear" should "clear" in {
    statsHandler.add("1", "mem")
    statsHandler.clear("1")
    val str : String = Await.result(statsHandler.show("1"), Duration.Inf)
    assert(str == "")
  }

  "Person with messages" should "have non-empty list of messages" in {
    statsHandler.add("1", "mem")
    statsHandler.add("2", "kek")
    val str : String = Await.result(statsHandler.show("1"), Duration.Inf)
    assert(str == "Link: mem")
    statsHandler.clear("1")
    statsHandler.clear("2")
  }

  "Messages from other people" should "be different" in {
    statsHandler.add("1", "kek1")
    statsHandler.add("1", "kek2")
    val str : String = Await.result(statsHandler.show("1"), Duration.Inf)
    assert(str == "Link: kek1\nLink: kek2")
    statsHandler.clear("1")
  }
}