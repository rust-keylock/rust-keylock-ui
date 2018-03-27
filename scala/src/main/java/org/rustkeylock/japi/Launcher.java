package org.rustkeylock.japi;

import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.astonbitecode.j4rs.api.invocation.NativeCallbackSupport;
import org.rustkeylock.Ui;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.utils.Defs;
import scalafx.stage.Stage;

public class Launcher extends NativeCallbackSupport implements EventHandler<WindowEvent> {
    public static Launcher start() {
        new Thread(() -> Ui.main(new String[]{})).start();
        return new Launcher();
    }

    public static Stage getStage() {
        Stage s = Ui.stage();
        if (s == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getStage();
        }
        return s;
    }

    // This is called from the native in order to activate asynchronous callback for the exit event
    public void initHandler() {
        // Call the getStage in order to implicitly wait for the initialization and set the OnCloseListener
        Stage stage = getStage();
        stage.onCloseRequest_$eq(this);
    }

    @Override
    public void handle(WindowEvent ev) {
        doCallback(GuiResponse.GoToMenu(Defs.MENU_EXIT()));
        ev.consume();
    }
}
