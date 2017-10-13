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

class ShowMessageCb(stage: Stage) extends RustCallback {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(message: String): Unit = {
    logger.debug(s"Callback for showing message $message")
    Platform.runLater(new UiThreadRunnable(message))
  }

  class UiThreadRunnable(message: String) extends Runnable {
    override def run(): Unit = {
      new Alert(AlertType.Information) {
        initOwner(stage)
        title = "rust-keylock"
        contentText = message
      }.showAndWait()
      // It doesn't matter which menu we return from the show message screen. The logic of the rust-keylock library only needs something to proceed. 
			InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, "")
    }
  }
}