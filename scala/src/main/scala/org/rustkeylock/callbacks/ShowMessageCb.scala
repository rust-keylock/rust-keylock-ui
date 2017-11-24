package org.rustkeylock.callbacks

import org.rustkeylock.api.RustCallback
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Alert
import org.rustkeylock.utils.Defs
import org.rustkeylock.api.InterfaceWithRust
import scalafx.stage.Stage
import org.rustkeylock.api.ShowMessageCallback
import org.rustkeylock.japi.ScalaUserOptionsSet
import scala.collection.JavaConverters.asScalaIterator
import org.rustkeylock.japi.ScalaUserOption
import scalafx.scene.control.ButtonType

class ShowMessageCb(stage: Stage) extends ShowMessageCallback {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(options: ScalaUserOptionsSet.ByReference, message: String, severity: String): Unit = {
    val opts = if (options.numberOfOptions == 1 && options.getOptions().get(0).label.equals(Defs.EMPTY_ARG)
      && options.getOptions().get(0).shortLabel.equals(Defs.EMPTY_ARG)
      && options.getOptions().get(0).value.equals(Defs.EMPTY_ARG)) {
      Nil
    } else {
      asScalaIterator(options.getOptions().iterator()).toList
    }
    logger.debug(s"Callback for showing message $message of severity $severity and options ${
      opts.map { opt =>
        {
          s"label: ${opt.label}, value: ${opt.value}, short label: ${opt.shortLabel}"
        }
      }.mkString(";")
    }")
    Platform.runLater(new UiThreadRunnable(message, severity, opts))
  }

  class UiThreadRunnable(message: String, severity: String, options: List[ScalaUserOption]) extends Runnable {
    private val alertType = severity match {
      case "Info" => AlertType.Information
      case "Warn" => AlertType.Warning
      case "Error" => AlertType.Error
      case other => {
        logger.error(s"Cannot handle severity $severity. Using the default (Info)")
        AlertType.Information
      }
    }

    val buttonTypesByOption = options.map { option =>
      {
        new ButtonType(option.label)
      }
    }

    override def run(): Unit = {
      val selectedButton = new Alert(alertType) {
        initOwner(stage)
        title = "rust-keylock"
        contentText = message
        buttonTypes = buttonTypesByOption
      }.showAndWait()

      selectedButton match {
        case Some(sb) => {
          val selectedUserOption = options.find(_.label == sb.getText)
          selectedUserOption match {
            case Some(uo) => InterfaceWithRust.INSTANCE.user_option_selected(uo.label, uo.value, uo.shortLabel)
            case None => {
              logger.error(s"Button ${sb.getText} does not exist in the User Options offered! How did it got here?? Please consider opening a bug to the developers.")
              InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_MAIN, Defs.EMPTY_ARG, "")
            }
          }
        }
        case None => InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_MAIN, Defs.EMPTY_ARG, "")
      }
    }
  }
}