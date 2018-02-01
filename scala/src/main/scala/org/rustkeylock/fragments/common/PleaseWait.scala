package org.rustkeylock.fragments.common

import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.VBox
import scalafx.scene.image.ImageView
import scalafx.scene.paint.LinearGradient
import scalafx.scene.effect.DropShadow
import scalafx.scene.text.Text
import scalafx.geometry.Pos
import org.rustkeylock.fragments.sides.Navigation
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy

class PleaseWait() extends Scene {
  
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