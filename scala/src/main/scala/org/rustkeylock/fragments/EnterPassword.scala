package org.rustkeylock.fragments

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.control.PasswordField
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane
import scalafx.scene.Scene
import scalafx.scene.layout.VBox
import scalafx.geometry.Pos
import scalafx.scene.text.Text
import scalafx.scene.control.Separator
import org.rustkeylock.components.RklButton
import scalafx.geometry.HPos
import org.rustkeylock.api.InterfaceWithRust
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import scalafx.scene.paint.Color
import scalafx.collections.ObservableBuffer
import org.rustkeylock.utils.SharedState

class EnterPassword extends Scene {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val password = new PasswordField() {
    promptText = "Password"
  }
  Platform.runLater(password.requestFocus())
  val passwordMessage = new Label

  val number = new PasswordField() {
    promptText = "Favorite number"
  }
  val numberMessage = new Label

  val label = new Text {
    text = "Welcome to rust-keylock"
    style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
  }
  GridPane.setHalignment(label, HPos.Center)

  val button = new RklButton {
    tooltip = "Decrypt"
    onAction = handle(buttonHandler)
    graphic = new ImageView("images/arrow_right.png")
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

    add(new Label("Please provide your password"), 0, 1)
    add(password, 1, 1)
    add(passwordMessage, 1, 2)

    add(new Label("What is your favorite number?"), 0, 3)
    add(number, 1, 3)
    add(numberMessage, 1, 4)

    add(button, 0, 5, 2, 1)

    add(image, 0, 6, 2, 1)
  }

  private def buttonHandler(): Unit = {
    passwordMessage.setText("")
    numberMessage.setText("")

    if (password.getText().trim().isEmpty()) {
			passwordMessage.setText("Required Field")
			passwordMessage.setTextFill(Color.Red)
		} else if (number.getText().trim().isEmpty()) {
			numberMessage.setText("Required Field")
			numberMessage.setTextFill(Color.Red)
		} else {
      try {
        val num = new Integer(number.getText().trim())
        InterfaceWithRust.INSTANCE.set_password(password.getText().trim(), num)
        SharedState.setLoggedIn()
      } catch {
        case error: Exception => {
          val message = "Incorrect number";
          logger.error(message, error);
          number.setText("");
          numberMessage.setText(message);
          numberMessage.setTextFill(Color.Red)
        }
      }
		}
  }
}
