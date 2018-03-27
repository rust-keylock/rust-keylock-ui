package org.rustkeylock.callbacks

import com.typesafe.scalalogging.Logger
import org.astonbitecode.j4rs.api.invocation.NativeCallbackSupport
import org.rustkeylock.fragments.ListEntries
import org.rustkeylock.japi.ScalaEntry
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters.asScalaIterator
import scalafx.application.Platform
import scalafx.stage.Stage

class ShowEntriesSetCb(stage: Stage) extends NativeCallbackSupport {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(entriesSet: java.util.List[ScalaEntry], filter: String): Unit = {
    logger.debug("Callback for showing Entries")
    val entries = asScalaIterator(entriesSet.iterator()).toList
    Platform.runLater(new UiThreadRunnable(stage, entries, filter))
  }

  class UiThreadRunnable(stage: Stage, entries: List[ScalaEntry], filter: String) extends Runnable {
    override def run(): Unit = {
      val processedFilter = if (filter == Defs.EMPTY_ARG) {
        ""
      } else {
        filter
      }
      stage.setScene(new ListEntries(entries.map(_.name), processedFilter, stage, doCallback))
    }
  }

}