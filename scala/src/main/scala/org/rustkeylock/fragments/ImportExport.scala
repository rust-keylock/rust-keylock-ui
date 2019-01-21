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

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.scalalogging.Logger
import javafx.scene.control.Separator
import javafx.scene.image.Image
import org.rustkeylock.callbacks.RklCallbackUpdateSupport
import org.rustkeylock.components.{RklButton, RklLabel}
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.stubs.GuiResponse
import org.slf4j.LoggerFactory
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.{PasswordField, ScrollPane, TextField}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, GridPane}
import scalafx.scene.text.Text
import scalafx.stage.{DirectoryChooser, FileChooser, Stage}

object ImportExport {
  def apply(export: Boolean, stage: Stage, callback: Object => Unit): ImportExport = {
    new ImportExport(export, stage, callback)
  }
}

case class ImportExport private(export: Boolean, stage: Stage, callback: Object => Unit) extends Scene with RklCallbackUpdateSupport[Scene] {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val homePath = System.getProperty("user.home")
  private val proposedFilename = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + "_rust_keylock"
  private var path = homePath + File.separator + proposedFilename

  override def withNewCallback(newCallback: Object => Unit): Scene = this.copy(callback = newCallback)

  root = new BorderPane() {
    style = "-fx-background: white"
    // Navigation pane
    left = Navigation(callback)
    // Main pane
    center = new ScrollPane {
      fitToHeight = true
      hbarPolicy = ScrollBarPolicy.AsNeeded
      vbarPolicy = ScrollBarPolicy.AsNeeded
      content = new Center
    }
  }

  class Center() extends GridPane {
    prefWidth <== stage.width - Navigation.Width

    private val title = new Text {
      text = if (export) {
        "Where to export?"
      } else {
        "What to import?"
      }
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)

    private val pathTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - 70
      promptText = "Path"
      text = path
    }
    Platform.runLater(pathTextField.end())

    val pathTextMessage = new RklLabel
    private val browseButton = new RklButton {
      tooltip = "Browse"
      onAction = handle {
        if (export) {
          browseDirectory()
        } else {
          browseFile()
        }
      }
      graphic = new ImageView {
        image = new Image("images/open.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(browseButton, HPos.Left)

    private val passwordField = new PasswordField() {
      prefWidth <== stage.width - Navigation.Width - 70
      promptText = "Use password"
    }
    val passwordFieldMessage = new RklLabel

    private val numberField = new PasswordField() {
      prefWidth <== stage.width - Navigation.Width - 70
      promptText = "Use favorite number"
    }
    val numberFieldMessage = new RklLabel

    private val okButton = new RklButton {
      tooltip = "Ok"
      onAction = handle(okButtonHandler())
      graphic = new ImageView {
        image = new Image("images/tick.png")
        fitHeight = 50
        fitWidth = 50
      }
    }
    GridPane.setHalignment(okButton, HPos.Center)

    vgap = 10
    padding = Insets(10, 10, 10, 10)
    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)
    add(new Separator(), 0, 1, 2, 1)

    add(new RklLabel("Path"), 0, 2)
    add(pathTextField, 0, 3)
    add(browseButton, 1, 3)
    add(pathTextMessage, 0, 4)

    if (!export) {
      add(new RklLabel("Use password"), 0, 5)
      add(passwordField, 0, 6)
      add(passwordFieldMessage, 0, 7)

      add(new RklLabel("Use favorite number"), 0, 8)
      add(numberField, 0, 9)
      add(numberFieldMessage, 0, 10)
    }

    add(okButton, 0, 11, 1, 3)

    private def browseFile(): Unit = {
      val fileChooser = new FileChooser {
        initialDirectory = new File(homePath)
      }
      val file = Option(fileChooser.showOpenDialog(stage))
      path = file.fold(path)(np => np.getAbsolutePath)
      logger.debug("Chosen file: " + path)
      pathTextField.setText(path)
    }

    private def browseDirectory(): Unit = {
      val dirChooser = new DirectoryChooser {
        initialDirectory = new File(homePath)
      }
      val dir = Option(dirChooser.showDialog(stage))
      path = dir.fold(path)(np => np.getAbsolutePath) + File.separator
      logger.debug("Chosen path: " + path)
      pathTextField.setText(path)
      Platform.runLater {
        pathTextField.requestFocus()
        pathTextField.end()
      }
    }

    private def okButtonHandler(): Unit = {
      pathTextMessage.clear()
      path = pathTextField.getText()
      if (export) {
        if (path.isEmpty()) {
          logger.debug(s"Exporting to $path")
          pathTextMessage.setError("Required Field")
        } else if (new File(path).isDirectory()) {
          pathTextMessage.setError("Cannot export to a directory")
        } else {
          callback(GuiResponse.ExportImport(path, 1, "Dummy", 11))
        }
      } else {
        passwordFieldMessage.clear()
        numberFieldMessage.clear()

        if (path.isEmpty()) {
          logger.debug(s"Importing from $path")
          pathTextMessage.setError("Required Field")
        } else if (new File(path).isDirectory()) {
          pathTextMessage.setError("Cannot import from a directory")
        } else if (passwordField.getText.isEmpty()) {
          passwordFieldMessage.setError("Required Field")
        } else if (numberField.getText.isEmpty()) {
          numberFieldMessage.setError("Required Field")
        } else {
          callback(GuiResponse.ExportImport(path, 0, passwordField.getText, Integer.parseInt(numberField.getText)))
        }
      }
    }
  }

}