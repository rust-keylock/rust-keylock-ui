package org.rustkeylock

import org.rustkeylock.fragments.Empty
import org.rustkeylock.init.NativeInitializer
import org.slf4j.LoggerFactory

import com.typesafe.scalalogging.Logger

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.image.Image
import scalafx.event.EventType
import scalafx.event.EventHandler
import scalafx.stage.WindowEvent
import scalafx.event.ActionEvent
import org.rustkeylock.utils.Defs
import org.rustkeylock.api.InterfaceWithRust
import scalafx.application.Platform

object Ui extends JFXApp {
  val Banner = """
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
    onCloseRequest = {
      new javafx.event.EventHandler[javafx.stage.WindowEvent] {
        def handle(ev: javafx.stage.WindowEvent): Unit = {
          InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXIT)
          ev.consume()
        }
      }

    }
  }

  stage.getIcons.add(new Image("images/rkl-small.png"))
  Platform.implicitExit_=(false)
  
  NativeInitializer.init(stage)
}