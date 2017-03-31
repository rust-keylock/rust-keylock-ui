package org.rustkeylock.fragments.main

import scalafx.scene.text.Text
import scalafx.scene.paint.LinearGradient
import scalafx.scene.effect.DropShadow
import scalafx.scene.paint.Stops
import scalafx.scene.paint.Color.Cyan
import scalafx.scene.paint.Color.DodgerBlue

class CenterPane extends Text {
  text = "rust-keylock"
  style = "-fx-font-size: 48pt"
  fill = new LinearGradient(
    endX = 0,
    stops = Stops(Cyan, DodgerBlue))
  effect = new DropShadow {
    color = DodgerBlue
    radius = 25
    spread = 0.25
  }
}