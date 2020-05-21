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
import slick.lifted.TableQuery

class BotStarter(override val client: RequestHandler[Future], val service: Service,
                 val userHandler: DBUserHandler,
                 val messageHandler: DBMessageHandler,
                 val statsHandler : DBStatsHandler) extends TelegramBot
  with Polling
  with Commands[Future] {

  onCommand("/start") { implicit msg =>
    msg.from match {
      case None => reply("Register error").void
      case Some(user) =>
        userHandler.register(user).flatMap(_ =>
          reply(s"You're registered.\nYour id is ${user.id}").void)
    }
  }

  onCommand("/get_id") { implicit msg =>
    msg.from match {
      case None => reply("Register error").void
      case Some(user) =>
        user.username match {
          case None => reply("pizdec").void
          case Some (str) => userHandler.getId (str).flatMap (it => reply (s"$it").void)
        }
    }
  }

  onCommand("/users") { implicit msg =>
    userHandler.show.flatMap(reply(_).void)
  }

  onCommand("/check") { implicit msg =>
    msg.from match {
      case None => reply("Error.").void
      case Some(user) =>
        val tmp = messageHandler.show(user.id.toString)
        messageHandler.clear(user.id.toString)
        for {
          messages <- tmp
          _ <- reply(messages).void
        } yield ()
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
    msg.from match {
      case None => reply("Cats error.").void
      case Some(user) =>
        service.getCat.flatMap(link => for {
          _ <- statsHandler.add(user.id.toString, link)
          _ <- reply(link)
        } yield ())
    }
  }

  onCommand("/stats") { implicit msg =>
    msg.from match {
      case None => reply("Stats error.").void
      case Some (x) => withArgs { args =>
        val id = if (args.isEmpty) {
          x.id.toString
        } else {
          val arg = args.head
          if (arg forall Character.isDigit) {
            arg
          } else {
            userHandler.getId(arg).toString
          }
        }
        statsHandler.show(id).flatMap(reply(_).void)
      }
    }
  }
}

object BotStarter {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val users = TableQuery[Users]
    val messages = TableQuery[Messages]
    val stats = TableQuery[Stats]
    val userHandler = new DBUserHandler(users, messages)
    val messageHandler = new DBMessageHandler(users, messages)
    val statsHandler = new DBStatsHandler(stats)

    val service: Service = new Service()

    val fileSource = Source.fromFile("token.txt")
    val token = fileSource.mkString
    fileSource.close()

    val init = for {
      _ <- userHandler.init()
      _ <- messageHandler.init()
      bot = new BotStarter(new FutureSttpClient(token), service,
        userHandler, messageHandler, statsHandler)
      _ <- bot.run()
    } yield ()
    Await.result(init, Duration.Inf)
  }
}