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
package org.rustkeylock.utils

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scalafx.scene.Node
import scalafx.scene.layout.FlowPane
import scalafx.stage.Screen

object Utils {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def flowPaneOf(nodes: List[Node]): FlowPane = {
    new FlowPane() {
      children = nodes
    }
  }

  def calculateXY(): (Double, Double) = {
    val x = (Screen.primary.visualBounds.getMinX(), Screen.primary.visualBounds.getMaxX()) match {
      case (min, max) if min <= Defs.PrefHeightPixels && max >= Defs.PrefWidthPixels => Defs.PrefWidthPixels
      case (_, max) => max
    }

    val y = (Screen.primary.visualBounds.getMinY(), Screen.primary.visualBounds.getMaxY()) match {
      case (min, max) if min <= Defs.PrefHeightPixels && max >= Defs.PrefHeightPixels => Defs.PrefHeightPixels
      case (_, max) => max
    }

    logger.info(s"Setting window width to $x and height to $y")
    (x, y)
  }
}
