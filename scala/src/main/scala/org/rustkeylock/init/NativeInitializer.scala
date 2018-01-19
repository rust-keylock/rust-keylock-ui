package org.rustkeylock.init

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.rustkeylock.api.InterfaceWithRust
import org.rustkeylock.callbacks.LogCb
import org.rustkeylock.callbacks.ShowEntriesSetCb
import org.rustkeylock.callbacks.ShowEntryCb
import org.rustkeylock.callbacks.ShowMenuCb
import org.rustkeylock.callbacks.ShowMessageCb
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger
import scalafx.stage.Stage
import scalafx.application.Platform
import org.rustkeylock.callbacks.EditConfigurationCb

object NativeInitializer {
  def init(stage: Stage): Unit = {
    new Thread(new NativeInitializer(stage)).start
  }
}

class NativeInitializer(stage: Stage) extends Runnable {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def run(): Unit = {
    logger.debug("Initializing rust-keylock native")
    Try {
      InterfaceWithRust.INSTANCE.execute(
        new ShowMenuCb(stage),
        new ShowEntryCb(stage),
        new ShowEntriesSetCb(stage),
        new ShowMessageCb(stage),
        new EditConfigurationCb(stage),
        new LogCb())
    } match {
      case Failure(error) => {
        logger.error("Native rust-keylock error detected", error)
      }
      case Success(_) => {
        logger.debug("Native rust-keylock exiting without errors")
      }
    }

    Platform.exit()
  }
}