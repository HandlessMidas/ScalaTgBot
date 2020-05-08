package bot

import scala.collection.mutable

class MessageHandler {
  var messagesUsers: mutable.Map[String, mutable.ListBuffer[(String, String)]] =
    mutable.Map[String, mutable.ListBuffer[(String, String)]]().withDefaultValue(mutable.ListBuffer())

  def send(senderId: String, receiverId: String, message: String): Unit = {
    if (!messagesUsers.contains(receiverId)) {
      messagesUsers(receiverId) = mutable.ListBuffer()
    }
    messagesUsers(receiverId) += Tuple2(senderId, message)
  }

  def show(id: String): String = messagesUsers(id).foldLeft(""){
    (last, p) => last + s"From: ${p._1}\n${p._2}\n\n"
  }

  def clear(id: String): Unit = messagesUsers(id).clear()
}