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
import scalafx.scene.control.TextField
import org.rustkeylock.components.RklLabel
import org.rustkeylock.components.RklButton
import scalafx.scene.image.ImageView
import javafx.scene.image.Image
import scalafx.stage.FileChooser
import scalafx.stage.Stage
import scalafx.scene.control.PasswordField
import scalafx.application.Platform
import java.text.SimpleDateFormat
import java.util.Date
import java.io.File
import scalafx.stage.DirectoryChooser
import scalafx.scene.paint.Color
import org.rustkeylock.api.InterfaceWithRust

class ImportExport(export: Boolean, stage: Stage) extends Scene {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val homePath = System.getProperty("user.home")
  val proposedFilename = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + "_rust_keylock"
  var path = homePath + File.separator + proposedFilename

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
    prefWidth <== stage.width - Navigation.Width

    val title = new Text {
      text = if (export) {
        "Where to export?"
      } else {
        "What to import?"
      }
      style = "-fx-font-size: 12pt;-fx-font-weight: bold;"
    }
    GridPane.setHalignment(title, HPos.Center)

    val pathTextField = new TextField() {
      prefWidth <== stage.width - Navigation.Width - 70
      promptText = "Path"
      text = path
    }
    Platform.runLater(pathTextField.end())

    val pathTextMessage = new RklLabel
    val browseButton = new RklButton {
      tooltip = "Browse"
      onAction = handle {
        if (export) {
          browseDirectory()
        } else {
          browseFile()
        }
      }
      graphic = new ImageView {
        image = new Image("images/open.png")
        fitHeight = 33
        fitWidth = 33
      }
    }
    GridPane.setHalignment(browseButton, HPos.Left)

    val passwordField = new PasswordField() {
      prefWidth <== stage.width - Navigation.Width - 70
      promptText = "Use password"
    }
    val passwordFieldMessage = new RklLabel

    val numberField = new PasswordField() {
      prefWidth <== stage.width - Navigation.Width - 70
      promptText = "Use favorite number"
    }
    val numberFieldMessage = new RklLabel

    val okButton = new RklButton {
      tooltip = "Ok"
      onAction = handle(okButtonHandler)
      graphic = new ImageView {
        image = new Image("images/tick.png")
        fitHeight = 50
        fitWidth = 50
      }
    }
    GridPane.setHalignment(okButton, HPos.Center)

    vgap = 10
    padding = Insets(10, 10, 10, 10)
    style = "-fx-background: white"

    add(title, 0, 0, 2, 1)

    add(new RklLabel("Path"), 0, 1)
    add(pathTextField, 0, 2)
    add(browseButton, 1, 2)
    add(pathTextMessage, 0, 3)

    if (!export) {
      add(new RklLabel("Use password"), 0, 4)
      add(passwordField, 0, 5)
      add(passwordFieldMessage, 0, 6)

      add(new RklLabel("Use favorite number"), 0, 7)
      add(numberField, 0, 8)
      add(numberFieldMessage, 0, 9)
    }

    add(okButton, 0, 10, 1, 3)

    private def browseFile(): Unit = {
      val fileChooser = new FileChooser {
        initialDirectory = new File(homePath)
      }
      val file = Option(fileChooser.showOpenDialog(stage))
      path = file.fold(path)(np => np.getAbsolutePath)
      logger.debug("Chosen file: " + path)
      pathTextField.setText(path)
    }

    private def browseDirectory(): Unit = {
      val dirChooser = new DirectoryChooser {
        initialDirectory = new File(homePath)
      }
      val dir = Option(dirChooser.showDialog(stage))
      path = dir.fold(path)(np => np.getAbsolutePath) + File.separator
      logger.debug("Chosen path: " + path)
      pathTextField.setText(path)
      Platform.runLater {
        pathTextField.requestFocus()
        pathTextField.end()
      }
    }

    private  def okButtonHandler(): Unit = {
      pathTextMessage.clear()
      path = pathTextField.getText()
      if(export) {
        if (path.isEmpty()) {
        	logger.debug(s"Exporting to $path")
          pathTextMessage.setError("Required Field")
        } else if (new File(path).isDirectory()) {
          pathTextMessage.setError("Cannot export to a directory")
        } else {
          InterfaceWithRust.INSTANCE.export_import(path, 1, "Dummy", 11)
        }
      } else {
        passwordFieldMessage.clear()
        numberFieldMessage.clear()

        if (path.isEmpty()) {
        	logger.debug(s"Importing from $path")
          pathTextMessage.setError("Required Field")
        } else if (new File(path).isDirectory()) {
          pathTextMessage.setError("Cannot import from a directory")
        } else if (passwordField.getText.isEmpty()) {
          passwordFieldMessage.setError("Required Field")
        } else if (numberField.getText.isEmpty()) {
          numberFieldMessage.setError("Required Field")
        } else {
          InterfaceWithRust.INSTANCE.export_import(path, 0, passwordField.getText, Integer.parseInt(numberField.getText))
        }
      }
    }
  }

}