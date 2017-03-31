package org.rustkeylock.utils

import scala.concurrent.stm.{ Ref, atomic }

object SharedState {
  private var loggedIn = Ref(false)

  def setLoggedIn(): Unit = {
    atomic { implicit txn =>
      {
        loggedIn() = true
      }
    }
  }

  def isLoggedIn(): Boolean = {
    atomic { implicit txn =>
      {
        loggedIn()
      }
    }
  }
}