package bot

import scala.concurrent.{Await, Future}
import slick.jdbc.H2Profile.api._
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global

class Stats(tag: Tag) extends Table[(Int, Int, String)](tag, "Stats") {
  def id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
  def userId = column[Int]("UserId")
  def link = column[String]("Link")
  def * = (id, userId, link)
}

class DBStatsHandler(stats: TableQuery[Stats]) {
  lazy val database : H2Profile.backend.Database = Database.forConfig("h2mem1")

  def init(): Future[Unit] = {
    database.run(stats.schema.createIfNotExists)
  }

  def add(userId : String, link : String): Future[Unit] = {
    val req = for {
      _ <- stats += (0, userId.toInt, link)
    } yield ()
    database.run(req)
  }

  def show(id: String): Future[String] = {
    val req = for {
      cats <- stats.filter(it => it.userId === id.toInt).result
    } yield cats
    database.run(req).flatMap(seq => Future(seq.map(it => s"Link: ${it._3}").mkString("\n")))
  }

  def clear(id: String): Future[Unit] = {
    database.run(stats.filter(_.userId === id.toInt).delete).flatMap(_ => Future())
  }
}
