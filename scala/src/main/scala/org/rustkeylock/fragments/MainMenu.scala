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
import org.rustkeylock.callbacks.RklCallbackUpdateSupport
import org.rustkeylock.fragments.sides.Navigation
import org.slf4j.LoggerFactory
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.text.Text
import scalafx.stage.Stage

object MainMenu {
  def apply(stage: Stage, callback: Object => Unit): MainMenu = {
    new MainMenu(stage, callback)
  }
}

case class MainMenu private(stage: Stage, callback: Object => Unit) extends Scene with RklCallbackUpdateSupport[Scene] {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

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

  private class Center extends VBox {
    spacing = 33
    padding = Insets(10, 10, 10, 10)
    alignment = Pos.Center

    children = Seq(
      new ImageView("images/rkl.png"),
      new Text {
        style = "-fx-font-size: 12pt"
        text = "Please make a selection"
      },
      new Text {
        style = "-fx-font-size: 12pt"
        text = "on the Navigation bar on the left in order to proceed"
      })
  }

}

