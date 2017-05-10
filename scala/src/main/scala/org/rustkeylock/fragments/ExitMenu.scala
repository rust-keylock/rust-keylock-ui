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
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.text.Text
import scalafx.geometry.HPos
import org.rustkeylock.components.RklLabel
import org.rustkeylock.components.RklButton
import scalafx.scene.image.ImageView
import scalafx.scene.image.Image
import org.rustkeylock.utils.Defs
import org.rustkeylock.api.InterfaceWithRust
import scalafx.stage.Stage
import scalafx.geometry.Pos

class ExitMenu(stage: Stage) extends Scene {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  root = new BorderPane() {
    style = "-fx-background: white"
    // Navigation pane
    left = new Navigation
    // Main pane
    center = new Center()
  }

  class Center() extends GridPane {
    val title = new Text {
      text = "Unsaved Data!"
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }

    GridPane.setHalignment(title, HPos.Center)

    val yesButton = new RklButton {
      tooltip = "Yes"
      onAction = handle {
        logger.debug("The User selected to force Exit with unsaved data")
        InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_FORCE_EXIT)
      }
      graphic = new ImageView {
        image = new Image("images/yes.png")
        fitHeight = 50
        fitWidth = 50
      }
    }
    GridPane.setHalignment(yesButton, HPos.Right)

    val noButton = new RklButton {
      tooltip = "No"
      onAction = handle {
        logger.debug("The User selected not to exit because of unsaved data")
        InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_MAIN)
      }
      graphic = new ImageView {
        image = new Image("images/no.png")
        fitHeight = 50
        fitWidth = 50
      }
    }
    GridPane.setHalignment(noButton, HPos.Left)

    vgap = 21
    padding = Insets(10, 10, 10, 10)
    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)
    add(new RklLabel("You will loose unsaved changes."), 0, 1, 2, 1)
    add(new RklLabel("Are you sure you want to exit?"), 0, 3, 2, 1)
    add(noButton, 0, 4)
    add(yesButton, 1, 4)
  }
}