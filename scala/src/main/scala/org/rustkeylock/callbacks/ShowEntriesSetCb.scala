package org.rustkeylock.callbacks

import org.rustkeylock.api.EntriesSetCallback
import org.rustkeylock.japi.ScalaEntriesSet
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import scalafx.stage.Stage
import scalafx.application.Platform
import org.rustkeylock.japi.ScalaEntry
import scala.collection.JavaConverters.asScalaIterator
import org.rustkeylock.fragments.ListEntries

class ShowEntriesSetCb(stage: Stage) extends EntriesSetCallback {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(entriesSet: ScalaEntriesSet.ByReference): Unit = {
    logger.debug("Callback for showing Entries")
    val entries = if (entriesSet.numberOfEntries == 1 && entriesSet.getEntries().get(0).name.equals("null")
				&& entriesSet.getEntries().get(0).user.equals("null")
				&& entriesSet.getEntries().get(0).pass.equals("null")
				&& entriesSet.getEntries().get(0).desc.equals("null")) {
			Nil
		} else {
			asScalaIterator(entriesSet.getEntries().iterator()).toList
		}

    Platform.runLater(new UiThreadRunnable(stage, entries))
  }

  class UiThreadRunnable(stage: Stage, entries: List[ScalaEntry]) extends Runnable {
    override def run(): Unit = {
      stage.setScene(new ListEntries(entries.map(_.name), stage))
    }
  }
}