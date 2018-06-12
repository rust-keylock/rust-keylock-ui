// Copyright 2017 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.
package org.rustkeylock.fragments

import com.typesafe.scalalogging.Logger
import org.rustkeylock.components.{RklButton, RklLabel}
import org.rustkeylock.japi.stubs.GuiResponse
import org.rustkeylock.utils.SharedState
import org.slf4j.LoggerFactory

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.Scene
import scalafx.scene.control.PasswordField
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane
import scalafx.scene.text.Text
import scalafx.stage.Stage

class EnterPassword(stage: Stage, callback: Object => Unit) extends Scene {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val password = new PasswordField() {
    promptText = "Password"
  }
  Platform.runLater(password.requestFocus())
  val passwordMessage = new RklLabel

  val number = new PasswordField() {
    promptText = "Favorite number"
  }
  val numberMessage = new RklLabel

  val label = new Text {
    text = "Welcome to rust-keylock"
    style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
  }
  GridPane.setHalignment(label, HPos.Center)

  val button = new RklButton {
    tooltip = "Decrypt"
    onAction = handle(buttonHandler)
    graphic = new ImageView("images/arrow_right.png")
    defaultButton = true
  }
  GridPane.setHalignment(button, HPos.Right)

  val image = new ImageView("images/rkl.png")
  GridPane.setHalignment(image, HPos.Center)

  root = new GridPane() {
    hgap = 33
    vgap = 10
    padding = Insets(10, 10, 10, 10)
    style = "-fx-background: white"

    add(label, 0, 0, 2, 1)

    add(new RklLabel("Please provide your password"), 0, 1)
    add(password, 1, 1)
    add(passwordMessage, 1, 2)

    add(new RklLabel("What is your favorite number?"), 0, 3)
    add(number, 1, 3)
    add(numberMessage, 1, 4)

    add(button, 0, 5, 2, 1)

    add(image, 0, 6, 2, 1)
  }

  private def buttonHandler(): Unit = {
    passwordMessage.clear()
    numberMessage.clear()

    if (password.getText().trim().isEmpty()) {
      passwordMessage.setError("Required Field")
    } else if (number.getText().trim().isEmpty()) {
      numberMessage.setError("Required Field")
    } else {
      try {
        val num = new Integer(number.getText().trim())
        callback(GuiResponse.ChangePassword(password.getText().trim(), num))
        SharedState.setLoggedIn()
      } catch {
        case error: Exception => {
          val message = "Incorrect number"
          logger.error(message, error)
          number.setText("")
          numberMessage.setError(message)
        }
      }
    }
  }
}
