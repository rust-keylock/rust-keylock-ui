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
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import org.rustkeylock.callbacks.RklCallbackUpdateSupport
import org.rustkeylock.components.RklButton
import org.rustkeylock.fragments.sides.Navigation
import org.rustkeylock.japi.stubs.GuiResponse
import org.rustkeylock.utils.{Defs, Utils}
import org.slf4j.LoggerFactory
import scalafx.Includes.handle
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{HPos, Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.{ListView, ScrollPane}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, GridPane}
import scalafx.scene.text.Text
import scalafx.stage.Stage

object ListEntries {
  def apply(entries: Seq[String], filter: String, stage: Stage, callback: Object => Unit): ListEntries = {
    val (x, y) = Utils.calculateXY()
    stage.setWidth(x)
    stage.setHeight(y)
    new ListEntries(entries, filter, stage, callback)
  }
}

case class ListEntries private(entries: Seq[String], filter: String, stage: Stage, callback: Object => Unit) extends Scene with RklCallbackUpdateSupport[Scene] {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  override def withNewCallback(newCallback: Object => Unit): Scene = this.copy(callback = newCallback)

  root = new BorderPane() {
    style = "-fx-background: white"
    // Navigation pane
    left = Navigation(callback)
    // Main pane
    center = new ScrollPane {
      alignmentInParent = Pos.TopCenter
      fitToHeight = true
      hbarPolicy = ScrollBarPolicy.AsNeeded
      vbarPolicy = ScrollBarPolicy.AsNeeded
      content = if (entries.nonEmpty) {
        new Center()
      } else {
        new CenterEmptyList
      }
    }
    onKeyPressed = new EventHandler[KeyEvent]() {
      def handle(event: KeyEvent) {
        if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
          val newFilter = filter + event.getText
          logger.debug(s"Filter changed to '$newFilter'")
          callback(GuiResponse.GoToMenuPlusArgs(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, newFilter))
        } else if (event.getCode.toString() == "BACK_SPACE") {
          val newFilter = filter.dropRight(1)
          logger.debug(s"Filter changed to '$newFilter'")
          callback(GuiResponse.GoToMenuPlusArgs(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, newFilter))
        } else {
          logger.debug(s"Ignoring pressed key of code ${event.getCode}")
        }
      }
    }
  }

  private class CenterEmptyList() extends GridPane {
    alignment = Pos.Center
    padding = Insets(10, 0, 0, 0)
    vgap = 7
    val title = new Text {
      text = "Passwords"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)
    val noEntriesLabel = new Text {
      text = "No Entries yet..."
      style = "-fx-font-size: 12pt;"
    }
    GridPane.setHalignment(noEntriesLabel, HPos.Center)
    val instLabel = new Text {
      text = "Click the plus button to add an entry"
      style = "-fx-font-size: 12pt;"
    }
    GridPane.setHalignment(instLabel, HPos.Center)

    val newButton = new RklButton {
      tooltip = "Add New"
      onAction = handle {
        logger.debug("The User Adds a new entry")
        callback(GuiResponse.GoToMenu(Defs.MENU_NEW_ENTRY))
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
    add(instLabel, 0, 2, 2, 1)
    val emptyList = new EntriesList()
    emptyList.setPlaceholder(noEntriesLabel)
    add(emptyList, 0, 4, 2, 1)
  }

  private class Center() extends GridPane {
    alignment = Pos.Center
    padding = Insets(10, 0, 0, 0)
    vgap = 11
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
        callback(GuiResponse.GoToMenu(Defs.MENU_NEW_ENTRY))
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

    val entriesList = new EntriesList()
    add(entriesList, 0, 3, 2, 1)
    entriesList.requestFocus()
  }

  private class EntriesList() extends ListView[String] {
    val logger = Logger(LoggerFactory.getLogger(this.getClass))

    prefWidth <== stage.width - Navigation.Width
    items = new ObservableBuffer[String] ++ (entries)
    onMouseClicked = handle {
      val pos = selectionModel().getSelectedIndex
      if (pos >= 0 && pos < entries.size) {
        logger.debug(s"Clicked entry with index $pos in the list of entries")
        callback(GuiResponse.GoToMenuPlusArgs(Defs.MENU_SHOW_ENTRY, pos.toString(), Defs.EMPTY_ARG))
      }
    }
  }

}