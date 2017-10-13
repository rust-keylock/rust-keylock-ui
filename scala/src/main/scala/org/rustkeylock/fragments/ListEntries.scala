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
import org.rustkeylock.components.RklLabel
import scalafx.stage.Stage
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.application.Platform

class ListEntries(entries: Seq[String], filter: String, stage: Stage) extends Scene {
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
    onKeyPressed = new EventHandler[KeyEvent]() {
      def handle(event: KeyEvent) {
        if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
          val newFilter = filter + event.getText
          logger.debug(s"Filter changed to '$newFilter'")
          InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, newFilter)
        } else if (event.getCode.toString() == "BACK_SPACE") {
          val newFilter = filter.dropRight(1)
          logger.debug(s"Filter changed to '$newFilter'")
          InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, newFilter)
        } else {
          logger.debug(s"Ignoring pressed key of code ${event.getCode}")
        }
      }
    }
  }

  private class Center() extends GridPane {
    padding = Insets(10, 0, 0, 0)
    val title = new Text {
      text = "Passwords"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)
    val subtitle = new Text {
      text = if (filter.isEmpty()) {
        "You can start typing in order to filter the list"
      } else {
        s"Currently applied filter: $filter"
      }
    }
    GridPane.setHalignment(subtitle, HPos.Center)

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
    add(subtitle, 0, 1, 2, 1)
    add(newButton, 1, 2)
    if (entries.nonEmpty) {
      val entriesList = new EntriesList()
      add(entriesList, 0, 3, 2, 1)
      entriesList.requestFocus()
    } else {
      add(new RklLabel("No entries"), 0, 3, 2, 1)
    }
  }

  private class EntriesList() extends ListView[String] {
    val logger = Logger(LoggerFactory.getLogger(this.getClass))

    prefWidth <== stage.width - Navigation.Width
    items = new ObservableBuffer[String] ++ (entries)
    onMouseClicked = handle {
      val pos = selectionModel().getSelectedIndex
      if (pos >= 0 && pos < entries.size) {
        logger.debug(s"Clicked entry with index $pos in the list of entries")
        InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_SHOW_ENTRY, pos.toString(), Defs.EMPTY_ARG)
      }
    }
  }
}