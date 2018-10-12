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
package org.rustkeylock

import com.typesafe.scalalogging.Logger
import org.rustkeylock.fragments.Empty
import org.slf4j.LoggerFactory
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.image.Image

object Ui extends JFXApp {
  val Banner =
    """
                _        _              _            _
 _ __ _   _ ___| |_     | | _____ _   _| | ___   ___| | __
| '__| | | / __| __|____| |/ / _ \ | | | |/ _ \ / __| |/ /
| |  | |_| \__ \ ||_____|   <  __/ |_| | | (_) | (__|   <
|_|   \__,_|___/\__|    |_|\_\___|\__, |_|\___/ \___|_|\_\
                                  |___/

"""
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  logger.info(Banner)

  stage = new PrimaryStage {
    title = "rust-keylock"
    scene = new Empty()
  }

  stage.getIcons.add(new Image("images/rkl-small.png"))
  Platform.implicitExit_=(false)

}
