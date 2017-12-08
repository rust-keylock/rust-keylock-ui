package org.rustkeylock.callbacks

import scala.collection.JavaConverters.asScalaIterator

import org.rustkeylock.api.StringListCallback
import org.rustkeylock.fragments.EditConfiguration
import org.rustkeylock.japi.StringList
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger

import scalafx.application.Platform
import scalafx.stage.Stage

class EditConfigurationCb(stage: Stage) extends StringListCallback {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def apply(strings: StringList.ByReference): Unit = {
    val scalaStrings = asScalaIterator(strings.getStrings.iterator()).toList
    logger.debug(scalaStrings.mkString(","));
    Platform.runLater(new UiThreadRunnable(scalaStrings))
  }

  class UiThreadRunnable(strings: List[String]) extends Runnable {

    override def run(): Unit = {
      stage.setScene(new EditConfiguration(strings, stage))
    }
  }
}