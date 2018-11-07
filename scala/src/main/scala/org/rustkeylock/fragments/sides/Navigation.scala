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
package org.rustkeylock.fragments.sides

import org.rustkeylock.callbacks.RklCallbackUpdateSupport
import org.rustkeylock.components.RklButton
import org.rustkeylock.japi.stubs.GuiResponse
import org.rustkeylock.utils.Defs
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.Tooltip.stringToTooltip
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.VBox

object Navigation {
  private[sides] val imgHeight = 64
  private[sides] val imgWidth = 64
  val Width: Int = imgWidth + 24

  def apply(callback: Object => Unit): Navigation = {
    new Navigation(callback)
  }
}

case class Navigation private(callback: Object => Unit) extends ScrollPane with RklCallbackUpdateSupport[ScrollPane] {
  override def withNewCallback(newCallback: Object => Unit): ScrollPane = this.copy(callback = newCallback)

  fitToHeight = true
  hbarPolicy = ScrollBarPolicy.AsNeeded
  vbarPolicy = ScrollBarPolicy.AsNeeded
  content = new NavigationContent(callback)
}

case class NavigationContent(callback: Object => Unit) extends VBox with RklCallbackUpdateSupport[VBox] {
  override def withNewCallback(newCallback: Object => Unit): VBox = this.copy(callback = newCallback)

  children = Seq(
    new RklButton {
      tooltip = "Passwords"
      onAction = handle(callback(GuiResponse.GoToMenuPlusArgs(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, "")))
      graphic = new ImageView {
        image = new Image("images/circled_list.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Encrypt and save"
      onAction = handle(callback(GuiResponse.GoToMenu(Defs.MENU_SAVE)))
      graphic = new ImageView {
        image = new Image("images/save.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Change master password"
      onAction = handle(callback(GuiResponse.GoToMenu(Defs.MENU_CHANGE_PASS)))
      graphic = new ImageView {
        image = new Image("images/edit.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Export to..."
      onAction = handle(callback(GuiResponse.GoToMenu(Defs.MENU_EXPORT_ENTRIES)))
      graphic = new ImageView {
        image = new Image("images/export.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Import from..."
      onAction = handle(callback(GuiResponse.GoToMenu(Defs.MENU_IMPORT_ENTRIES)))
      graphic = new ImageView {
        image = new Image("images/importimg.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Edit Configuration..."
      onAction = handle(callback(GuiResponse.GoToMenu(Defs.MENU_SHOW_CONFIGURATION)))
      graphic = new ImageView {
        image = new Image("images/settings.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Exit"
      onAction = handle(callback(GuiResponse.GoToMenu(Defs.MENU_EXIT)))
      graphic = new ImageView {
        image = new Image("images/close.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    })
}