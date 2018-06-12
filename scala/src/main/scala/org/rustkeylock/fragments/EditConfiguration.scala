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

import javafx.scene.image.Image

import com.typesafe.scalalogging.Logger
import org.rustkeylock.components.{RklButton, RklLabel}
import org.rustkeylock.fragments.common.PleaseWait
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.stubs.GuiResponse
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scalafx.Includes.{handle, jfxImage2sfx}
import scalafx.application.Platform
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.{CheckBox, PasswordField, ScrollPane, TextField}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, GridPane}
import scalafx.scene.text.Text
import scalafx.stage.Stage

class EditConfiguration(strings: List[String], stage: Stage, callback: Object => Unit) extends Scene {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  root = new BorderPane() {
    style = "-fx-background: white"
    // Navigation pane
    left = new Navigation(callback)
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

    val passwordTextField = new PasswordField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Password"
      text = strings(2)
    }
    val passwordMessage = new RklLabel

    val selfSignedCertCheckBox = new CheckBox("Use a self-signed certificate") {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      selected = strings(3).toBoolean
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
    GridPane.setHalignment(okButton, HPos.Left)

    val cancelButton = new RklButton {
      tooltip = "Cancel"
      onAction = handle(callback(GuiResponse.GoToMenu(Defs.MENU_MAIN)))
      graphic = new ImageView {
        image = new Image("images/close.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(cancelButton, HPos.Right)

    val syncButton = new RklButton {
      tooltip = "Synchronize now"
      onAction = handle(handleSynchronize)
      graphic = new ImageView {
        image = new Image("images/synchronize.png")
        fitHeight = 22
        fitWidth = 22
      }
    }
    GridPane.setHalignment(okButton, HPos.Left)

    hgap = 33
    vgap = 7
    padding = Insets(PaddingValue, PaddingValue, PaddingValue, PaddingValue)
    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)
    add(subtitleNextcloud, 0, 2)
    add(syncButton, 1, 2)

    add(new RklLabel("Server URL"), 0, 3, 2, 1)
    add(urlTextField, 0, 4, 2, 1)
    add(urlMessage, 0, 5)

    add(new RklLabel("Username"), 0, 6, 2, 1)
    add(usernameTextField, 0, 7, 2, 1)
    add(usernameMessage, 0, 8)

    add(new RklLabel("Password"), 0, 9, 2, 1)
    add(passwordTextField, 0, 10, 2, 1)
    add(passwordMessage, 0, 11)

    add(selfSignedCertCheckBox, 0, 12, 2, 2)
    add(derCertLocationMessage, 0, 14)

    add(okButton, 0, 18, 1, 1)
    add(cancelButton, 1, 18, 1, 1)

    private def handleOk(): Unit = {
      urlMessage.clear()

      val strings = ListBuffer(urlTextField.getText, usernameTextField.getText, passwordTextField.getText, selfSignedCertCheckBox.isSelected().toString())

      logger.debug(s"Applying Configuration with Strings: ${strings.mkString(",")}")

      callback(GuiResponse.SetConfiguration(bufferAsJavaList(strings)))
    }

    private def handleSynchronize(): Unit = {
      callback(GuiResponse.GoToMenu(Defs.MENU_SYNCHRONIZE))
      Platform.runLater(new PleaseWaitRunnable)
    }

    class PleaseWaitRunnable() extends Runnable {
      override def run(): Unit = {
        stage.setScene(new PleaseWait(callback))
      }
    }

  }

}