package org.rustkeylock.fragments

import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import org.rustkeylock.fragments.main.CenterPane
import org.rustkeylock.fragments.sides.Navigation

class PostLogin extends Scene {
  root = new BorderPane() {
    style = "-fx-background: white"
    // Main pane
    center = new CenterPane
    // Navigation pane
    left = new Navigation
  }
}