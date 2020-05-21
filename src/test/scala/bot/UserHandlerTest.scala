package bot

import com.bot4s.telegram.models.User
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import slick.lifted.TableQuery

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class UserHandlerTest extends AnyFlatSpec with Matchers {
  val usersT = TableQuery[Users]
  val messagesT = TableQuery[Messages]

  val userHandler = new DBUserHandler(usersT)
  userHandler.init()

  val users : List[User] = List(
    User(0, isBot = false, "Shrek", Some("1")),
    User(1, isBot = false, "Shrek", Some("2")),
    User(2, isBot = false, "Shrek", Some("3")),
    User(3, isBot = false, "Donkey", Some("Kong")),
    User(4, isBot = false, "Mario", None)
  )

  val usersDescriptions : List[String] = List(
    "Shrek 1, id: 0\n",
    "Shrek 2, id: 1\n",
    "Shrek 3, id: 2\n",
    "Donkey Kong, id: 3\n",
    "Mario , id: 4\n"
  )

  "Register" should "add Users" in {
    users.foreach{ userHandler.register }
    val tmp: Array[String] = Await.result(userHandler.show, Duration.Inf).split('\n')
    assert(tmp.length == 5)
  }

  "Users" should "have description" in {
    userHandler.register(users(0))
    userHandler.register(users(2))
    userHandler.register(users(4))
    val tmp: Array[String] = Await.result(userHandler.show, Duration.Inf).split('\n')
    assert(tmp.contains(usersDescriptions(0).dropRight(1)))
    assert(tmp.contains(usersDescriptions(2).dropRight(1)))
    assert(tmp.contains(usersDescriptions(4).dropRight(1)))
  }
}
