package bot

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.{FutureSttpClient, ScalajHttpClient}
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models.{Message, User}
import com.softwaremill.sttp.{SttpBackendOptions, sttp}
import com.softwaremill.sttp.json4s.asJson
import com.softwaremill.sttp.okhttp.{OkHttpBackend, OkHttpFutureBackend}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.util.Random
import scala.io.Source
import com.softwaremill.sttp._
import com.softwaremill.sttp.json4s._
import org.json4s.native.Serialization

class BotStarter(override val client: RequestHandler[Future], val service: Service,
                 val userHandler: UserHandler, val messageHandler: MessageHandler) extends TelegramBot
  with Polling
  with Commands[Future] {

  onCommand("/start") { implicit msg =>
    msg.from match {
      case None => reply("Register error").void
      case Some(user) =>
        userHandler.register(user)
        reply(s"You're registered.\nYour id is ${user.id}").void
    }
  }

  onCommand("/users") { implicit msg =>
    reply(userHandler.show()).void
  }

  onCommand("/check") { implicit msg =>
    msg.from match {
      case None => reply("Error.").void
      case Some(user) =>
        val messages = messageHandler.show(user.id.toString)
        messageHandler.clear(user.id.toString)
        if (messages.nonEmpty) reply(messages).void
        else reply("You haven't received any new messages yet.").void
    }
  }

  onCommand("/send") { implicit msg =>
    var from_id : Int = 0
    msg.from match {
      case None => from_id = 0
      case Some(user) => from_id = user.id
    }

    msg.text match {
      case None => reply("Enter non-empty message").void
      case Some(s) =>
        val id: String = s.slice(6, 15)
        val text: String = s.slice(16, s.length)
        messageHandler.send(from_id.toString, id, text)
        reply(s"Message was sent to $id").void
    }
  }

  onCommand("/cat") { implicit msg =>
    service.getCat.flatMap(reply(_)).void
  }
}

object BotStarter {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val userHandler = new UserHandler
    val messageHandler = new MessageHandler
    val fileSource = Source.fromFile("token.txt")
    val token = fileSource.mkString
    fileSource.close()

    val service: Service = new Service()
    val bot = new BotStarter(new FutureSttpClient(token), service, userHandler, messageHandler)
    Await.result(bot.run(), Duration.Inf)
  }
}