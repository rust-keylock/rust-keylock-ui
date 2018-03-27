package org.rustkeylock.callbacks

import com.typesafe.scalalogging.Logger
import org.astonbitecode.j4rs.api.invocation.NativeCallbackSupport
import org.rustkeylock.fragments.ShowEntry
import org.rustkeylock.japi.ScalaEntry
import org.slf4j.LoggerFactory

import scalafx.application.Platform
import scalafx.stage.Stage

class ShowEntryCb(stage: Stage) extends NativeCallbackSupport {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(anEntry: ScalaEntry, entryIndex: Integer, edit: java.lang.Boolean, delete: java.lang.Boolean): Unit = {
    logger.debug(s"Callback for showing Entry with index $entryIndex")
    Platform.runLater(new UiThreadRunnable(stage, anEntry, entryIndex, edit, delete))
  }

  class UiThreadRunnable(stage: Stage, anEntry: ScalaEntry, entryIndex: Int, edit: Boolean, delete: Boolean) extends Runnable {
    override def run(): Unit = {
      stage.setScene(new ShowEntry(anEntry, entryIndex, edit, delete, stage, doCallback))
    }
  }

}