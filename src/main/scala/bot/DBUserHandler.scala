package bot

import scala.concurrent.{Await, Future}
import slick.jdbc.H2Profile.api._
import com.bot4s.telegram.models.User
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class Users(tag: Tag) extends Table[(Int, String, String)](tag, "Messages"){
  def id = column[Int]("Id", O.PrimaryKey)
  def name = column[String]("Name")
  def username = column[String]("Username")
  def * = (id, name, username)
}

class DBUserHandler(users: TableQuery[Users]) {
  lazy val database = Database.forConfig("h2mem1")

  def init(): Future[Unit] = {
    database.run(users.schema.createIfNotExists)
  }

  def register(user: User): Future[Unit] = {
    val req = for {
      _ <- users += (user.id, user.firstName + " " + user.lastName.getOrElse(""), user.username.getOrElse(""))
    } yield ()
    database.run(req)
  }

  def show: Future[String] = {
    val req = for {
      allUsers <- users.result
    } yield allUsers
    database.run(req).flatMap(seq => Future(seq.map(it => s"${it._2}, id: ${it._1}").mkString("\n")))
  }

  def getId(username: String): Future[Int] = {
    val req = for {
      user <- users.filter(it => it.username === username).result.head
    } yield user
    database.run(req).flatMap(it => Future(it._1))
  }
}
