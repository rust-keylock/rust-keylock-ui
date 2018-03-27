package org.rustkeylock.callbacks

import com.typesafe.scalalogging.Logger
import org.astonbitecode.j4rs.api.invocation.NativeCallbackSupport
import org.rustkeylock.fragments.EditConfiguration
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters.asScalaIterator
import scalafx.application.Platform
import scalafx.stage.Stage

class EditConfigurationCb(stage: Stage) extends NativeCallbackSupport {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(strings: java.util.List[String]): Unit = {
    val scalaStrings = asScalaIterator(strings.iterator()).toList
    logger.debug(scalaStrings.mkString(","))
    Platform.runLater(new UiThreadRunnable(scalaStrings))
  }

  class UiThreadRunnable(strings: List[String]) extends Runnable {

    override def run(): Unit = {
      stage.setScene(new EditConfiguration(strings, stage, doCallback))
    }
  }

}