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
import org.rustkeylock.fragments.ShowEntry
import org.rustkeylock.japi.ScalaEntry
import org.slf4j.LoggerFactory
import scalafx.application.Platform
import scalafx.stage.Stage

class ShowEntryCb(stage: Stage) extends NativeCallbackToRustChannelSupport {
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