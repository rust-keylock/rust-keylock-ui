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
package org.rustkeylock.callbacks

import com.typesafe.scalalogging.Logger
import org.astonbitecode.j4rs.api.invocation.NativeCallbackToRustChannelSupport
import org.rustkeylock.components.RklStage
import org.rustkeylock.japi.ScalaUserOption
import org.rustkeylock.japi.stubs.{GuiResponse, ScalaMenu}
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.layout.Region

import scala.collection.JavaConverters.asScalaIterator

class ShowMessageCb(stage: RklStage) extends NativeCallbackToRustChannelSupport {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(options: java.util.List[ScalaUserOption], message: String, severity: String): Unit = {
    val opts = asScalaIterator(options.iterator()).toList

    logger.debug(s"Callback for showing message $message of severity $severity and options ${
      opts.map { opt => {
        s"label: ${opt.label}, value: ${opt.value}, short label: ${opt.short_label}"
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

    private val buttonTypesByOption = options.map { option => {
      new ButtonType(option.label)
    }
    }

    override def run(): Unit = {
      val alert = new Alert(alertType) {
        initOwner(stage.fxStage())
        title = "rust-keylock"
        contentText = message
        buttonTypes = buttonTypesByOption
      }
      alert.getDialogPane.setMinHeight(Region.USE_PREF_SIZE)
      val selectedButton = alert.showAndWait()

      selectedButton match {
        case Some(sb) => {
          val selectedUserOption = options.find(_.label == sb.getText)
          selectedUserOption match {
            case Some(uo) => {
              doCallback(GuiResponse.UserOptionSelected(uo))
            }
            case None => {
              logger.error(s"Button ${sb.getText} does not exist in the User Options offered! How did it got here?? Please consider opening a bug to the developers.")
              doCallback(GuiResponse.GoToMenu(ScalaMenu.Main()))
            }
          }
        }
        case None => doCallback(GuiResponse.GoToMenu(ScalaMenu.Main()))
      }
    }
  }

}