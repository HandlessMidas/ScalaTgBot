package bot

import com.softwaremill.sttp
import com.softwaremill.sttp.SttpBackend
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class ServiceTest extends AnyFlatSpec with Matchers with MockFactory {
  trait mocks {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = mock[SttpBackend[Future, Nothing]]

    val service = new Service()
  }

  "Service" should "return link with cat" in new mocks {
    (backend.send[Response] _).expects(*).returning(Future.successful(
      sttp.Response.ok(Response(List(Data(link = "cat"))))
    ))
    val result: String = Await.result(service.getCat, Duration.Inf)
    assert(result == "cat")
  }
}
