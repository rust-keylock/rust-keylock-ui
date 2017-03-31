package org.rustkeylock.callbacks

import org.rustkeylock.api.EntryCallback
import org.rustkeylock.fragments.ShowEntry
import org.rustkeylock.japi.ScalaEntry
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger

import scalafx.application.Platform
import scalafx.stage.Stage

class ShowEntryCb(stage: Stage) extends EntryCallback {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(anEntry: ScalaEntry.ByReference, entryIndex: Int, edit: Boolean, delete: Boolean): Unit = {
    logger.debug(s"Callback for showing Entry with index $entryIndex")
    Platform.runLater(new UiThreadRunnable(stage, anEntry, entryIndex, edit, delete))
  }

  class UiThreadRunnable(stage: Stage, anEntry: ScalaEntry.ByReference, entryIndex: Int, edit: Boolean, delete: Boolean) extends Runnable {
    override def run(): Unit = {
      stage.setScene(new ShowEntry(anEntry, entryIndex, edit, delete))
    }
  }
}