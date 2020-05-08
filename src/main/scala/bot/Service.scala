package bot

import com.softwaremill.sttp.{SttpBackendOptions, sttp}
import com.softwaremill.sttp.json4s.asJson

import scala.concurrent.{Await, ExecutionContext, Future}
import com.softwaremill.sttp._
import org.json4s.native.Serialization

case class Response(data: List[Data])
case class Data(link: String)

class Service(implicit val backend: SttpBackend[Future, Nothing],
              implicit val serialization : Serialization.type = org.json4s.native.Serialization,
              implicit val ec: ExecutionContext = ExecutionContext.global) {
  val request: RequestT[Id, Response, Nothing] = sttp
    .header("Authorization", "Client-ID 2a47c24862afdf7")
    .get(uri"https://api.imgur.com/3/gallery/search?q=cats")
    .response(asJson[Response])

  def getCat: Future[String] = backend.send(request).map { response =>
    scala.util.Random.shuffle(response.unsafeBody.data).head.link
  }
}