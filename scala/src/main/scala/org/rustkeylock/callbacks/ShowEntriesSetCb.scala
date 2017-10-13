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
import org.rustkeylock.utils.Defs

class ShowEntriesSetCb(stage: Stage) extends EntriesSetCallback {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(entriesSet: ScalaEntriesSet.ByReference, filter: String): Unit = {
    logger.debug("Callback for showing Entries")
    val entries = if (entriesSet.numberOfEntries == 1 && entriesSet.getEntries().get(0).name.equals(Defs.EMPTY_ARG)
				&& entriesSet.getEntries().get(0).user.equals(Defs.EMPTY_ARG)
				&& entriesSet.getEntries().get(0).pass.equals(Defs.EMPTY_ARG)
				&& entriesSet.getEntries().get(0).desc.equals(Defs.EMPTY_ARG)) {
			Nil
		} else {
			asScalaIterator(entriesSet.getEntries().iterator()).toList
		}

    Platform.runLater(new UiThreadRunnable(stage, entries, filter))
  }

  class UiThreadRunnable(stage: Stage, entries: List[ScalaEntry], filter: String) extends Runnable {
    override def run(): Unit = {
      val processedFilter = if(filter == Defs.EMPTY_ARG) {
        ""
      } else {
        filter
      }
      stage.setScene(new ListEntries(entries.map(_.name), processedFilter, stage))
    }
  }
}