package bot

import com.bot4s.telegram.models.User

import scala.collection.mutable


class UserHandler {
  def getUserDescription(user : User) = s"${user.firstName} ${user.lastName.getOrElse("")}, id: ${user.id}\n"

  val registeredUsers: mutable.Set[User] = mutable.Set[User]()

  def register(user: User): Unit = registeredUsers += user

  def show(): String = registeredUsers.foldLeft("") {
    (last, user) => last + getUserDescription(user)
  }
}