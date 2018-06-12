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

import java.net.{MalformedURLException, URL}

import javafx.scene.image.Image
import com.typesafe.scalalogging.Logger
import org.rustkeylock.components.{RklButton, RklLabel}
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.ScalaEntry
import org.rustkeylock.japi.stubs.GuiResponse
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory
import scalafx.Includes._
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.{ScrollPane, TextArea, TextField}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, GridPane}
import scalafx.scene.text.Text
import scalafx.stage.Stage

import scala.util.{Failure, Try}

class ShowEntry(anEntry: ScalaEntry,
                entryIndex: Int,
                edit: Boolean,
                delete: Boolean,
                stage: Stage,
                callback: Object => Unit) extends Scene {
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
      text = "Password Details"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)

    val titleTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Entry Title"
      text = anEntry.name
      editable = edit
      disable = !edit
    }
    val titleMessage = new RklLabel

    val urlTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "https://example.com"
      text = anEntry.url
      editable = edit
      disable = !edit
    }
    val urlMessage = new RklLabel

    val usernameTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Username"
      text = anEntry.user
      editable = edit
      disable = !edit
    }
    val usernameMessage = new RklLabel

    val passwordTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Password"
      text = anEntry.pass
      editable = edit
    }
    val passwordMessage = new RklLabel

    val descriptionTextArea = new TextArea() {
      prefWidth <== stage.width - Navigation.Width - PaddingValue - PaddingValue
      promptText = "Description"
      text = anEntry.desc
      editable = edit
      disable = !edit
    }

    val editButton = new RklButton {
      tooltip = "Edit"
      onAction = handle(callback(GuiResponse.GoToMenuPlusArgs(Defs.MENU_EDIT_ENTRY, entryIndex.toString(), Defs.EMPTY_ARG)))
      graphic = new ImageView {
        image = new Image("images/edit.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(editButton, HPos.Left)

    val deleteButton = new RklButton {
      tooltip = "Delete"
      onAction = handle(callback(GuiResponse.GoToMenuPlusArgs(Defs.MENU_DELETE_ENTRY, entryIndex.toString(), Defs.EMPTY_ARG)))
      graphic = new ImageView {
        image = new Image("images/delete.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(deleteButton, HPos.Right)

    val okButton = new RklButton {
      tooltip = "Ok"
      onAction = handle(handleOk)
      graphic = new ImageView {
        image = new Image("images/tick.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(okButton, HPos.Right)

    val areYouSureButton = new RklButton {
      tooltip = "Yes I am sure, delete it!"
      onAction = handle(callback(GuiResponse.DeleteEntry(entryIndex)))
      graphic = new ImageView {
        image = new Image("images/caution.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(areYouSureButton, HPos.Right)

    hgap = 33
    vgap = 10
    padding = Insets(PaddingValue, PaddingValue, PaddingValue, PaddingValue)
    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)

    add(new RklLabel("Entry title"), 0, 2, 2, 1)
    add(titleTextField, 0, 3, 2, 1)
    add(titleMessage, 0, 4)

    add(new RklLabel("URL"), 0, 5, 2, 1)
    add(urlTextField, 0, 6, 2, 1)
    add(urlMessage, 0, 7)

    add(new RklLabel("Username"), 0, 8, 2, 1)
    add(usernameTextField, 0, 9, 2, 1)
    add(usernameMessage, 0, 10)

    add(new RklLabel("Password"), 0, 11, 2, 1)
    add(passwordTextField, 0, 12, 2, 1)
    add(passwordMessage, 0, 13)

    add(new RklLabel("Description"), 0, 14, 2, 1)
    add(descriptionTextArea, 0, 15, 2, 1)

    (edit, delete) match {
      case (false, false) => {
        add(editButton, 0, 16)
        add(deleteButton, 1, 16)
      }
      case (true, false) => {
        add(okButton, 1, 16)
      }
      case (false, true) => {
        add(new RklLabel("Deleting Entry... Are you sure?"), 0, 16)
        add(areYouSureButton, 1, 16)
      }
      case (_, _) => throw new Exception(s"Cannot handle edit '$edit' and delete '$delete'. Please consider opening a bug to the developers.")
    }

    private def handleOk(): Unit = {
      titleMessage.clear()
      usernameMessage.clear()
      passwordMessage.clear()

      val entry = new ScalaEntry()
      entry.name = titleTextField.getText()
      entry.url = urlTextField.getText()
      entry.user = usernameTextField.getText()
      entry.pass = passwordTextField.getText()
      entry.desc = descriptionTextArea.getText()

      logger.debug(s"Saving entry ${entry.name}")

      var errorsExist = false
      if (entry.name.isEmpty()) {
        titleMessage.setError("Required Field")
        errorsExist = true
      }
      if (entry.user.isEmpty()) {
        usernameMessage.setError("Required Field")
        errorsExist = true
      }
      if (entry.pass.isEmpty()) {
        passwordMessage.setError("Required Field")
        errorsExist = true
      }
      if (entry.url.nonEmpty) {
        Try(new URL((entry.url))) match {
          case Failure(error) => if (error.getClass == classOf[MalformedURLException]) {
            urlMessage.setError("Wrong URL. Eg: https://my.com")
            errorsExist = true
          }
          case _: Any => {
            // ignore
          }
        }
      }
      if (!errorsExist) {
        if (entryIndex >= 0) {
          callback(GuiResponse.ReplaceEntry(entry, entryIndex))
        } else {
          callback(GuiResponse.AddEntry(entry))
        }
      }
    }
  }

}