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
import org.rustkeylock.fragments._
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory
import scalafx.application.Platform
import scalafx.stage.Stage

class ShowMenuCb(stage: Stage) extends NativeCallbackToRustChannelSupport {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(menu: String): Unit = {
    logger.debug("Callback for showing menu " + menu)
    Platform.runLater(new UiThreadRunnable(stage, menu))
  }

  class UiThreadRunnable(stage: Stage, menu: String) extends Runnable {
    override def run(): Unit = {
      val newScene = menu match {
        case Defs.MENU_TRY_PASS => {
          new EnterPassword(stage, doCallback)
        }
        case Defs.MENU_CHANGE_PASS => {
          new ChangePassword(stage, doCallback)
        }
        case Defs.MENU_MAIN => {
          new MainMenu(stage, doCallback)
        }
        case Defs.MENU_EXIT => {
          new ExitMenu(stage, doCallback)
        }
        case Defs.MENU_EXPORT_ENTRIES => {
          new ImportExport(true, stage, doCallback)
        }
        case Defs.MENU_IMPORT_ENTRIES => {
          new ImportExport(false, stage, doCallback)
        }
        case other => throw new RuntimeException(s"Cannot Show Menu with name '$menu' and no arguments")
      }

      stage.setScene(newScene)

    }
  }

}