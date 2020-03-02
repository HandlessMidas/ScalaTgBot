package bot

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.{FutureSttpClient, ScalajHttpClient}
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models.{Message, User}
import com.softwaremill.sttp.SttpBackendOptions
import com.softwaremill.sttp.okhttp.{OkHttpBackend, OkHttpFutureBackend}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

class BotStarter(override val client: RequestHandler[Future]) extends TelegramBot
  with Polling
  with Commands[Future] {

  val registeredUsers: mutable.Set[User] = mutable.Set[User]()

  onCommand("/start") { implicit msg =>
    msg.from match {
      case None => reply("Register error").void
      case Some(user) => {
          registeredUsers += user
          reply("You're registered.\n Your id is ${user.id}").void
      }
    }
  }

  onCommand("/users") { implicit msg =>
    var usersString = ""
    registeredUsers.foreach {
      it => usersString += s"${it.firstName} ${it.lastName}\n"
    }
    reply(usersString).void
  }
}

object BotStarter {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val fileSource = Source.fromFile("token.txt")
    val token = fileSource.mkString
    fileSource.close()
    val bot = new BotStarter(new FutureSttpClient(token))
    Await.result(bot.run(), Duration.Inf)
  }
}