package org.rustkeylock.components

import javafx.scene.image.ImageView
import scalafx.scene.control.TextField
import scalafx.scene.layout.AnchorPane

object RklTextFieldWithButton {
  def apply(textField: TextField, button: RklButton): RklTextFieldWithButton = {
    new RklTextFieldWithButton(textField, button)
  }
}

case class RklTextFieldWithButton(textField: TextField, button: RklButton) extends AnchorPane {
  val PaddingValue = 10
  val RightAnchorValue = 3.0

  if (Option(button.getGraphic).isDefined) {
    button.getGraphic match {
      case iv: ImageView => {
        textField.setPrefHeight(iv.getImage.getHeight + PaddingValue)
      }
      case _: Any => // ignore
    }
  }

  children = List(button, textField)

  AnchorPane.setRightAnchor(button, RightAnchorValue)
}
