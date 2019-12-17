// Copyright 2019 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.
package org.rustkeylock.ui.callbacks;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.astonbitecode.j4rs.api.invocation.NativeCallbackToRustChannelSupport;
import org.rustkeylock.controllers.*;
import org.rustkeylock.fxcomponents.RklStage;
import org.rustkeylock.ui.Defs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class ShowMenuCb extends NativeCallbackToRustChannelSupport {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RklStage stage;

    public ShowMenuCb(RklStage rklStage) {
        this.stage = rklStage;
    }

    public void apply(String menu) {
        logger.debug("Callback for showing menu " + menu);
        Platform.runLater(() -> {
            try {
                URL resurl;
                FXMLLoader loader = new FXMLLoader();

                switch (menu) {
                    case Defs.MENU_PLEASE_WAIT:
                        resurl = getClass().getResource("/fragments/please_wait.fxml");
                        break;
                    case Defs.MENU_TRY_PASS:
                        resurl = getClass().getResource("/fragments/enter_password.fxml");
                        loader.setControllerFactory(clazz -> new EnterPasswordController(stage));
                        break;
                    case Defs.MENU_CHANGE_PASS:
                        resurl = getClass().getResource("/fragments/change_password.fxml");
                        loader.setControllerFactory(clazz -> new ChangePasswordController(stage));
                        break;
                    case Defs.MENU_EXPORT_ENTRIES:
                        resurl = getClass().getResource("/fragments/import_export.fxml");
                        loader.setControllerFactory(clazz -> new ImportExportController(true, stage.getFxStage()));
                        break;
                    case Defs.MENU_IMPORT_ENTRIES:
                        resurl = getClass().getResource("/fragments/import_export.fxml");
                        loader.setControllerFactory(clazz -> new ImportExportController(false, stage.getFxStage()));
                        break;
                    case Defs.MENU_EXIT:
                        resurl = getClass().getResource("/fragments/exit.fxml");
                        loader.setControllerFactory(clazz -> new ExitController());
                        break;
                    case Defs.MENU_CURRENT:
                        resurl = null;
                        break;
                    case Defs.MENU_MAIN:
                    default:
                        resurl = getClass().getResource("/fragments/menu_main.fxml");
                        loader.setControllerFactory(clazz -> new MainMenuController());
                        break;

                }

                // If the controller is null here, it means that we should not change view.
                // In this case, we should retrieve the currently active controller in order to set a new callback for it.
                RklController controller = null;
                if (resurl != null) {
                    loader.setLocation(resurl);
                    Parent root = loader.load();

                    controller = loader.getController();
                    Scene scene = new Scene(root);
                    stage.updateView(scene, controller);
                } else {
                    controller = stage.getCurrentlyActiveController().orElseThrow(() -> new RuntimeException("Saved state not found!"));
                }
                controller.setCallback(this::doCallback);

            } catch (IOException error) {
                error.printStackTrace();
            }
        });
    }
}
