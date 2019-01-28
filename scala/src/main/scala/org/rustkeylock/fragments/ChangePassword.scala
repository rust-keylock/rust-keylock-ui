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
import javafx.scene.control.Separator
import org.rustkeylock.callbacks.RklCallbackUpdateSupport
import org.rustkeylock.components.{RklButton, RklLabel}
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.stubs.GuiResponse
import org.rustkeylock.utils.SharedState
import org.slf4j.LoggerFactory
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{HPos, Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.{PasswordField, ScrollPane}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, GridPane}
import scalafx.scene.text.Text
import scalafx.stage.Stage

object ChangePassword {
  def apply(stage: Stage, callback: Object => Unit): ChangePassword = {
    new ChangePassword(stage, callback)
  }
}

case class ChangePassword private(stage: Stage, callback: Object => Unit) extends Scene with RklCallbackUpdateSupport[Scene] {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  override def withNewCallback(newCallback: Object => Unit): Scene = this.copy(callback = newCallback)

  val CenterWidth = if (SharedState.isLoggedIn()) {
    stage.width - Navigation.Width
  } else {
    stage.width
  }

  root = new BorderPane() {
    style = "-fx-background: white"
    if (SharedState.isLoggedIn()) {
      // Navigation pane
      left = Navigation(callback)
    }
    // Main pane
    center = new ScrollPane {
      fitToHeight = true
      hbarPolicy = ScrollBarPolicy.AsNeeded
      vbarPolicy = ScrollBarPolicy.AsNeeded
      content = new Center
    }
  }

  class Center extends GridPane {
    padding = Insets(33, 0, 0, 0)
    vgap = 11
    alignment = Pos.TopCenter
    prefWidth <== CenterWidth

    private val password1 = new PasswordField() {
      prefWidth <== CenterWidth
      promptText = "Password"
    }
    Platform.runLater(password1.requestFocus())
    val passwordMessage1 = new RklLabel
    private val password2 = new PasswordField() {
      prefWidth <== CenterWidth
      promptText = " Re-enter password"
    }
    val passwordMessage2 = new RklLabel

    private val number1 = new PasswordField() {
      prefWidth <== CenterWidth
      promptText = "Favorite number"
    }
    val numberMessage1 = new RklLabel
    private val number2 = new PasswordField() {
      prefWidth <== CenterWidth
      promptText = "Re-enter favorite number"
    }
    val numberMessage2 = new RklLabel

    private val label = new Text {
      text = "Master password setup"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(label, HPos.Center)

    private val applyButton = new RklButton {
      tooltip = "Apply"
      onAction = handle(buttonHandler())
      graphic = new ImageView("images/arrow_right.png")
      stylesheets = new ObservableBuffer[String]()
      defaultButton = true
    }

    GridPane.setHalignment(applyButton, HPos.Right)

    hgap = 33
    vgap = 10
    padding = Insets(10, 10, 10, 10)
    style = "-fx-background: white"

    add(label, 0, 0, 2, 1)
    add(new Separator(), 0, 1, 2, 1)

    add(new RklLabel("Please provide your password"), 0, 2)
    add(password1, 0, 3)
    add(passwordMessage1, 0, 4)

    add(new RklLabel("Re-enter your password"), 0, 5)
    add(password2, 0, 6)
    add(passwordMessage2, 0, 7)

    add(new RklLabel("What is your favorite number?"), 0, 8)
    add(number1, 0, 9)
    add(numberMessage1, 0, 10)

    add(new RklLabel("Re-enter your favorite number"), 0, 11)
    add(number2, 0, 12)
    add(numberMessage2, 0, 13)

    add(applyButton, 0, 14, 2, 1)

    private def buttonHandler(): Unit = {
      passwordMessage1.clear()
      passwordMessage2.clear()
      numberMessage1.clear()
      numberMessage2.clear()

      if (password1.getText() != password2.getText()) {
        passwordMessage2.setError("The provided passwords did not match")
      } else if (number1.getText() != number2.getText()) {
        numberMessage2.setError("The provided favorite numbers did not match")
      } else {
        if (password1.getText().trim().isEmpty) {
          passwordMessage1.setError("This Field cannot be empty")
          password1.clear()
        } else if (number1.getText().trim().isEmpty) {
          numberMessage1.setError("This Field cannot be empty")
          number1.clear()
        } else {
          try {
            logger.info("Password and number changed")
            callback(GuiResponse.ChangePassword(password1.getText().trim(), number1.getText().trim().toInt))
            SharedState.setLoggedIn()
          } catch {
            case error: Exception => {
              val message = "Incorrect number"
              logger.error(message, error)
              number1.setText("")
              numberMessage1.setError(message)
            }
          }
        }
      }
    }
  }

}