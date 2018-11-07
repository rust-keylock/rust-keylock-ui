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
package org.rustkeylock.fragments.common

import org.rustkeylock.callbacks.RklCallbackUpdateSupport
import org.rustkeylock.fragments.sides.Navigation
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.text.Text
import scalafx.stage.Stage

object PleaseWait {
  def apply(callback: Object => Unit): PleaseWait = {
    new PleaseWait(callback)
  }
}

case class PleaseWait private(callback: Object => Unit) extends Scene with RklCallbackUpdateSupport[Scene] {
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

  class Center() extends VBox() {
    style = "-fx-background: white"
    alignment = Pos.Center
    children = Seq(
      new ImageView("images/wait2.gif"),
      new Text {
        text = "Please Wait..."
        style = "-fx-font-size: 24pt"
      })
  }

}