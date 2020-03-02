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

import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

class BotStarter(override val client: RequestHandler[Future]) extends TelegramBot
  with Polling
  with Commands[Future]{

}