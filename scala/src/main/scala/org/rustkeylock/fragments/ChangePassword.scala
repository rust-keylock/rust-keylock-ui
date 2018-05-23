package org.rustkeylock.fragments

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.layout.GridPane
import scalafx.geometry.HPos
import org.slf4j.LoggerFactory

import scalafx.application.Platform
import com.typesafe.scalalogging.Logger

import scalafx.scene.control.PasswordField
import org.rustkeylock.components.RklLabel

import scalafx.scene.text.Text
import org.rustkeylock.components.RklButton

import scalafx.scene.image.ImageView
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.layout.BorderPane
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.stubs.GuiResponse

import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import org.rustkeylock.utils.SharedState

import scalafx.stage.Stage

class ChangePassword(stage: Stage, callback: Object => Unit) extends Scene {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  root = new BorderPane() {
    style = "-fx-background: white"
    if (SharedState.isLoggedIn) {
      // Navigation pane
      left = new Navigation(callback)
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
    prefWidth <== stage.width - Navigation.Width

    val password1 = new PasswordField() {
      promptText = "Password"
    }
    Platform.runLater(password1.requestFocus())
    val passwordMessage1 = new RklLabel
    val password2 = new PasswordField() {
      promptText = " Re-enter password"
    }
    val passwordMessage2 = new RklLabel

    val number1 = new PasswordField() {
      promptText = "Favorite number"
    }
    val numberMessage1 = new RklLabel
    val number2 = new PasswordField() {
      promptText = "Re-enter favorite number"
    }
    val numberMessage2 = new RklLabel

    val label = new Text {
      text = "Master password setup"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(label, HPos.Center)

    val applyButton = new RklButton {
      tooltip = "Apply"
      onAction = handle(buttonHandler)
      graphic = new ImageView("images/arrow_right.png")
      stylesheets = new ObservableBuffer[String]()
    }

    GridPane.setHalignment(applyButton, HPos.Right)

    hgap = 33
    vgap = 10
    padding = Insets(10, 10, 10, 10)
    style = "-fx-background: white"

    add(label, 0, 0, 2, 1)

    add(new RklLabel("Please provide your password"), 0, 1)
    add(password1, 1, 1)
    add(passwordMessage1, 1, 2)

    add(new RklLabel("Re-enter your password"), 0, 3)
    add(password2, 1, 3)
    add(passwordMessage2, 1, 4)

    add(new RklLabel("What is your favorite number?"), 0, 5)
    add(number1, 1, 5)
    add(numberMessage1, 1, 6)

    add(new RklLabel("Re-enter your favorite number"), 0, 7)
    add(number2, 1, 7)
    add(numberMessage2, 1, 8)

    add(applyButton, 0, 9, 2, 1)

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
        if (password1.getText().trim().isEmpty()) {
          passwordMessage1.setError("This Field cannot be empty")
          password1.clear()
        } else if (number1.getText().trim().isEmpty()) {
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