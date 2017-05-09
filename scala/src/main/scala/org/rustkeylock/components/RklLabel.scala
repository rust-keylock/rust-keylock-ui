package org.rustkeylock.components

import scalafx.scene.control.Label
import scalafx.scene.paint.Color

class RklLabel(initialMessage: String) extends Label {

  def this() = this("")

  this.setText(initialMessage)

  def setError(message: String): Unit = {
    text = message
		textFill = Color.Red
  }

  def clear(): Unit = this.setText("")
}