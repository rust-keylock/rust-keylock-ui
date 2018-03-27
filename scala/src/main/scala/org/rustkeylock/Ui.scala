package org.rustkeylock

import com.typesafe.scalalogging.Logger
import org.astonbitecode.j4rs.api.invocation.NativeCallbackSupport
import org.rustkeylock.fragments.Empty
import org.rustkeylock.japi.stubs.GuiResponse
import org.rustkeylock.utils.Defs
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

  def initOnCloseRequest(callback: Object => Unit): Unit = {
    stage.setOnCloseRequest(new OnCloseHandler(callback))
  }

}

class OnCloseHandler(callback: Object => Unit) extends javafx.event.EventHandler[javafx.stage.WindowEvent] {
  override def handle(ev: javafx.stage.WindowEvent): Unit = {
    callback(GuiResponse.GoToMenu(Defs.MENU_EXIT))
    ev.consume()
  }
}
