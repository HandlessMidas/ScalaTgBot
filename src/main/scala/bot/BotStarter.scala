package bot

import bot.BotStarter.Service
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

class BotStarter(override val client: RequestHandler[Future], val service: Service) extends TelegramBot
  with Polling
  with Commands[Future] {

  val registeredUsers: mutable.Set[User] = mutable.Set[User]()
  var messagesUsers: mutable.Map[String, mutable.ListBuffer[(String, String)]] = mutable.Map[String, mutable.ListBuffer[(String, String)]]().withDefaultValue(mutable.ListBuffer())

  onCommand("/start") { implicit msg =>
    msg.from match {
      case None => reply("Register error").void
      case Some(user) =>
        registeredUsers += user
        reply(s"You're registered.\nYour id is ${user.id}").void
    }
  }

  onCommand("/users") { implicit msg =>
    var usersString = ""
    registeredUsers.foreach {
      it => usersString += s"${it.firstName} ${it.lastName}\n"
    }
    reply(usersString).void
  }

  onCommand("/cat") { implicit msg =>
    service.getCat().flatMap(reply(_)).void
  }

  onCommand("/check") { implicit msg =>
    msg.from match {
      case None => reply("Error.").void
      case Some(user) =>
        messagesUsers(user.id.toString()).foreach { p =>
          reply(s"From: ${p._1}\n${p._2}\n\n")
        }
        messagesUsers(user.id.toString).clear()
        reply("").void
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
        if (!messagesUsers.contains(id)) {
          messagesUsers(id) = mutable.ListBuffer()
        }
        messagesUsers(id) += Tuple2(from_id.toString, text)
        reply(s"Message was sent to $id").void
    }
  }
}

object BotStarter {

  implicit val serialization =  org.json4s.native.Serialization

  case class Response(data: List[Data])
  case class Data(link: String)

  class Service(implicit val backend: SttpBackend[Future, Nothing]) {
    implicit val ec: ExecutionContext = ExecutionContext.global
    val request: RequestT[Id, Response, Nothing] = sttp
      .header("Authorization", "Client-ID 2a47c24862afdf7")
      .get(uri"https://api.imgur.com/3/gallery/search?q=cats")
      .response(asJson[Response])

    def getCat() = backend.send(request).map { response =>
      scala.util.Random.shuffle(response.unsafeBody.data).head.link
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val fileSource = Source.fromFile("token.txt")
    val token = fileSource.mkString
    fileSource.close()

    val service: Service = new Service()
    val bot = new BotStarter(new FutureSttpClient(token), service)
    Await.result(bot.run(), Duration.Inf)
  }
}