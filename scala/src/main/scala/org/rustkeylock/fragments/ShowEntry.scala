package org.rustkeylock.fragments

import scalafx.Includes._
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.ScalaEntry
import scalafx.geometry.HPos
import scalafx.geometry.Insets
import scalafx.scene.Scene
import org.rustkeylock.components.RklLabel
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.TextField
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.GridPane
import scalafx.scene.text.Text
import org.rustkeylock.components.RklButton
import scalafx.scene.image.ImageView
import javafx.scene.image.Image
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import org.rustkeylock.api.InterfaceWithRust
import org.rustkeylock.utils.Defs
import scalafx.scene.paint.Color

class ShowEntry(anEntry: ScalaEntry.ByReference, entryIndex: Int, edit: Boolean, delete: Boolean) extends Scene {
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
    val title = new Text {
      text = "Password Details"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)

    val titleTextField = new TextField() {
      promptText = "Entry Title"
      text = anEntry.name
      editable = edit
    }
    val titleMessage = new RklLabel

    val usernameTextField = new TextField() {
      promptText = "Username"
      text = anEntry.user
      editable = edit
    }
    val usernameMessage = new RklLabel

    val passwordTextField = new TextField() {
      promptText = "Password"
      text = anEntry.pass
      editable = edit
    }
    val passwordMessage = new RklLabel

    val descriptionTextField = new TextField() {
      promptText = "Description"
      text = anEntry.desc
      editable = edit
    }

    val editButton = new RklButton {
      tooltip = "Edit"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_EDIT_ENTRY, entryIndex))
      graphic = new ImageView {
        image = new Image("images/edit.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(editButton, HPos.Left)

    val deleteButton = new RklButton {
      tooltip = "Delete"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_DELETE_ENTRY, entryIndex))
      graphic = new ImageView {
        image = new Image("images/delete.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(deleteButton, HPos.Right)

    val saveButton = new RklButton {
      tooltip = "Save"
      onAction = handle(handleSave)
      graphic = new ImageView {
        image = new Image("images/save.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(saveButton, HPos.Right)

    val areYouSureButton = new RklButton {
      tooltip = "Yes I am sure, delete it!"
      onAction = handle(InterfaceWithRust.INSTANCE.delete_entry(entryIndex))
      graphic = new ImageView {
        image = new Image("images/caution.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(areYouSureButton, HPos.Right)

    hgap = 33
    vgap = 10
    padding = Insets(10, 10, 10, 10)
    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)

    add(new RklLabel("Entry title"), 0, 2, 2, 1)
    add(titleTextField, 0, 3, 2, 1)
    add(titleMessage, 0, 4)

    add(new RklLabel("Username"), 0, 5, 2, 1)
    add(usernameTextField, 0, 6, 2, 1)
    add(usernameMessage, 0, 7)

    add(new RklLabel("Password"), 0, 8, 2, 1)
    add(passwordTextField, 0, 9, 2, 1)
    add(passwordMessage, 0, 10)

    add(new RklLabel("Description"), 0, 11, 2, 1)
    add(descriptionTextField, 0, 12, 2, 1)

    (edit, delete) match {
      case (false, false) => {
        add(editButton, 0, 13)
        add(deleteButton, 1, 13)
      }
      case (true, false) => {
        add(saveButton, 1, 13)
      }
      case (false, true) => {
        add(new RklLabel("Deleting Entry... Are you sure?"), 0, 13)
        add(areYouSureButton, 1, 13)
      }
      case (_, _) => throw new Exception(s"Cannot handle edit '$edit' and delete '$delete'. Please consider opening a bug to the developers.")
    }

    private def handleSave(): Unit = {
      titleMessage.clear()
      usernameMessage.clear()
      passwordMessage.clear()

      val entry = new ScalaEntry()
      entry.name = titleTextField.getText()
      entry.user = usernameTextField.getText()
      entry.pass = passwordTextField.getText()
      entry.desc = descriptionTextField.getText()

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
      if (!errorsExist) {
        if (entryIndex >= 0) {
          InterfaceWithRust.INSTANCE.replace_entry(entry, entryIndex);
        } else {
          InterfaceWithRust.INSTANCE.add_entry(entry);
        }
      }
    }
  }
}