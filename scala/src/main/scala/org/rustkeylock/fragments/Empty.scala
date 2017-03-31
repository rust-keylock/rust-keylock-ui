package org.rustkeylock.fragments

import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.VBox
import scalafx.scene.image.ImageView
import scalafx.scene.paint.LinearGradient
import scalafx.scene.effect.DropShadow
import scalafx.scene.text.Text
import scalafx.geometry.Pos

class Empty extends Scene {
  root = new VBox() {
    style = "-fx-background: white"
    alignment = Pos.Center
    children = Seq(
      new ImageView("images/rkl.png"),
      new Text {
        text = "rust-keylock"
        style = "-fx-font-size: 48pt"
      })
  }
}