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
import org.rustkeylock.fragments.ListEntries
import org.rustkeylock.japi.ScalaEntry
import org.rustkeylock.utils.Defs
import org.slf4j.LoggerFactory
import scalafx.application.Platform
import scalafx.stage.Stage

import scala.collection.JavaConverters.asScalaIterator

class ShowEntriesSetCb(stage: Stage) extends NativeCallbackToRustChannelSupport {
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