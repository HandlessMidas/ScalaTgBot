package bot

import slick.jdbc.H2Profile
import scala.concurrent.{Await, Future}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class Messages(tag: Tag) extends Table[(Int, String, Int, Int)](tag, "Messages"){
  def id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
  def message = column[String]("Message")
  def senderId = column[Int]("SenderId")
  def receiverId = column[Int]("ReceiverId")
  def * = (id, message, senderId, receiverId)
}

class DBMessageHandler(messages: TableQuery[Messages]) {
  lazy val database = Database.forConfig("h2mem1")

  def init(): Future[Unit] = {
    database.run(messages.schema.createIfNotExists)
  }

  def send(senderId: String, receiverId: String, message: String): Future[Unit] = {
    val req = for {
      _ <- messages += (-1, message, senderId.toInt, receiverId.toInt)
    } yield()
    database.run(req)
  }

  def show(id: String): Future[String] = {
    val req = for {
      idMessages <- messages.filter(it => it.receiverId === id.toInt).result
    } yield idMessages
    database.run(req).flatMap(seq => Future (
      seq.map(it => s"From: ${it._3}\n${it._2}\n").mkString("\n"))
    )
  }

    def clear(id: String): Future[Unit] = {
      database.run(messages.delete).flatMap(_ => Future())
    }
}
