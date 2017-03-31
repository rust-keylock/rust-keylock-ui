package org.rustkeylock.fragments

import org.rustkeylock.api.InterfaceWithRust
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger

import scalafx.Includes.handle
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.ListView
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.GridPane
import scalafx.geometry.HPos
import scalafx.scene.text.Text
import scalafx.geometry.Insets
import org.rustkeylock.components.RklButton
import scalafx.scene.image.ImageView
import scalafx.scene.image.Image
import scalafx.scene.control.Label

class ListEntries(entries: Seq[String]) extends Scene {
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
      content = new Center()
    }
  }

  private class Center() extends GridPane {
    val title = new Text {
      text = "Passwords"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)

    val newButton = new RklButton {
      tooltip = "Add New"
      onAction = handle {
        logger.debug("The User Adds a new entry")
        InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_NEW_ENTRY)
      }
      graphic = new ImageView {
        image = new Image("images/newimage.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(newButton, HPos.Right)

    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)
    add(newButton, 1, 1)
    if (entries.nonEmpty) {
      add(new EntriesList(), 0, 2, 2, 1)
    } else {
      add(new Label("No entries"), 0, 2, 2, 1)
    }
  }

  private class EntriesList() extends ListView[String] {
    val logger = Logger(LoggerFactory.getLogger(this.getClass))

    items = new ObservableBuffer[String] ++ (entries)
    onMouseClicked = handle {
      val pos = selectionModel().getSelectedIndex
      logger.debug(s"Clicked entry with index $pos in the list of entries")
      InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_SHOW_ENTRY, pos)
    }

  }
}