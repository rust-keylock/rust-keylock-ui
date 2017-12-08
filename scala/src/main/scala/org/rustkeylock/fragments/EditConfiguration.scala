package org.rustkeylock.fragments

import org.rustkeylock.api.InterfaceWithRust
import org.rustkeylock.components.RklButton
import org.rustkeylock.components.RklLabel
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.StringList
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory

import com.sun.jna.StringArray
import com.typesafe.scalalogging.Logger

import javafx.scene.image.Image
import scalafx.Includes.handle
import scalafx.Includes.jfxImage2sfx
import scalafx.geometry.HPos
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.TextField
import scalafx.scene.image.ImageView
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.GridPane
import scalafx.scene.text.Text
import scalafx.stage.Stage

class EditConfiguration(strings: List[String], stage: Stage) extends Scene {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  root = new BorderPane() {
    style = "-fx-background: white"
    // Navigation pane
    left = new Navigation
    // Main pane
    center = new ScrollPane {
      fitToHeight = true
      hbarPolicy = ScrollBarPolicy.AsNeeded
      vbarPolicy = ScrollBarPolicy.AsNeeded
      content = new Center
    }
  }

  class Center() extends GridPane {
    val PaddingValue = 10

    val title = new Text {
      text = "Configuration"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)
    val subtitleNextcloud = new Text {
      text = "Nextcloud"
      style = "-fx-font-size: 10pt;-fx-font-weight: bold;"
    }

    val urlTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Server URL"
      text = strings(0)
    }
    val urlMessage = new RklLabel

    val usernameTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Username"
      text = strings(1)
    }
    val usernameMessage = new RklLabel

    val passwordTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Password"
      text = strings(2)
    }
    val passwordMessage = new RklLabel

    val derCertLocationTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Self-signed certificate DER location"
      text = strings(3)
    }
    val derCertLocationMessage = new RklLabel

    val okButton = new RklButton {
      tooltip = "Ok"
      onAction = handle(handleOk)
      graphic = new ImageView {
        image = new Image("images/ok.png")
        fitHeight = 40
        fitWidth = 40
      }
    }
    GridPane.setHalignment(okButton, HPos.Right)
    val cancelButton = new RklButton {
      tooltip = "Cancel"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN))
      graphic = new ImageView {
        image = new Image("images/close.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(cancelButton, HPos.Left)

    hgap = 33
    vgap = 7
    padding = Insets(PaddingValue, PaddingValue, PaddingValue, PaddingValue)
    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)
    add(subtitleNextcloud, 0, 2, 2, 1)

    add(new RklLabel("Server URL"), 0, 3, 2, 1)
    add(urlTextField, 0, 4, 2, 1)
    add(urlMessage, 0, 5)

    add(new RklLabel("Username"), 0, 6, 2, 1)
    add(usernameTextField, 0, 7, 2, 1)
    add(usernameMessage, 0, 8)

    add(new RklLabel("Password"), 0, 9, 2, 1)
    add(passwordTextField, 0, 10, 2, 1)
    add(passwordMessage, 0, 11)

    add(new RklLabel("Self-signed certificate DER location"), 0, 12, 2, 1)
    add(derCertLocationTextField, 0, 13, 2, 1)
    add(derCertLocationMessage, 0, 14)

    add(okButton, 0, 18)
    add(cancelButton, 1, 18)

    private def handleOk(): Unit = {
      urlMessage.clear()

      val strArr = Array(urlTextField.getText, usernameTextField.getText, passwordTextField.getText, derCertLocationTextField.getText)

      logger.debug(s"Applying Configuration with Strings: ${strArr.mkString(",")}")

      var errorsExist = false
      if (strArr(0).isEmpty()) {
        urlMessage.setError("Required Field")
        errorsExist = true
      }
      if (strArr(1).isEmpty()) {
        usernameMessage.setError("Required Field")
        errorsExist = true
      }
      if (strArr(2).isEmpty()) {
        passwordMessage.setError("Required Field")
        errorsExist = true
      }

      if (!errorsExist) {
        val strings = new StringList.ByReference()
        strings.strings = new StringArray(strArr)
        strings.numberOfstrings = strArr.size
        InterfaceWithRust.INSTANCE.set_configuration(strings);
      }
    }
  }
}