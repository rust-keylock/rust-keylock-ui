package org.rustkeylock.callbacks

import org.rustkeylock.api.RustCallback
import org.rustkeylock.fragments.EnterPassword
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger

import scalafx.application.Platform
import scalafx.stage.Stage
import org.rustkeylock.fragments.Empty
import org.rustkeylock.fragments.MainMenu
import org.rustkeylock.fragments.ImportExport
import org.rustkeylock.fragments.ExitMenu
import org.rustkeylock.fragments.ChangePassword

class ShowMenuCb(stage: Stage) extends RustCallback {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(menu: String): Unit = {
    logger.debug("Callback for showing menu " + menu)
    Platform.runLater(new UiThreadRunnable(stage, menu))
  }

  class UiThreadRunnable(stage: Stage, menu: String) extends Runnable {
    override def run(): Unit = {
      val newScene = menu match {
        case Defs.MENU_TRY_PASS => {
          new EnterPassword()
        }
        case Defs.MENU_CHANGE_PASS => {
          new ChangePassword()
        }
        case Defs.MENU_MAIN => {
          new MainMenu()
        }
        case Defs.MENU_EXIT => {
          new ExitMenu()
        }
        case Defs.MENU_EXPORT_ENTRIES => {
          new ImportExport(true, stage)
        }
        case Defs.MENU_IMPORT_ENTRIES => {
          new ImportExport(false, stage)
        }
        case other => throw new RuntimeException(s"Cannot Show Menu with name '$menu' and no arguments")
      }

      stage.setScene(newScene)

    }
  }
}