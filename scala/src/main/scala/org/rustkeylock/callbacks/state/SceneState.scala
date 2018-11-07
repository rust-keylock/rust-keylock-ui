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
package org.rustkeylock.callbacks.state

import java.util.concurrent.atomic.AtomicReference

import org.rustkeylock.callbacks.RklCallbackUpdateSupport
import scalafx.scene.Scene

private[rustkeylock] object SceneState extends RklState[Scene] {
  private val prevScene: AtomicReference[Option[Scene]] = new AtomicReference(None)

  override def set(scene: Scene): Unit = {
    prevScene.set(Some(scene))
  }

  override def get(): Option[Scene] = {
    prevScene.get()
  }

  override def clear(): Unit = {
    prevScene.set(None)
  }

  def getWithUpdatedCallback(callback: Object => Unit): Scene = {
    get().getOrElse(throw new RuntimeException("No saved state for Scene exists yet. Cannot update callback")) match {
      case withCallbackChangeSupport: RklCallbackUpdateSupport[Scene] => withCallbackChangeSupport.withNewCallback(callback)
      case other: Scene => other
    }
  }
}
