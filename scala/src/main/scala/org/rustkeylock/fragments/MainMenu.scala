package org.rustkeylock.fragments

import scalafx.Includes._
import scalafx.scene.Scene
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import scalafx.scene.layout.GridPane
import scalafx.geometry.Insets
import scalafx.scene.layout.BorderPane
import org.rustkeylock.fragments.sides.Navigation
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.VBox
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.ListView
import scalafx.scene.text.Text
import scalafx.scene.paint.Stops
import scalafx.geometry.Pos
import scalafx.scene.image.ImageView

class MainMenu extends Scene {
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

