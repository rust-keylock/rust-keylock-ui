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
import org.rustkeylock.controllers.ListEntriesController;
import org.rustkeylock.controllers.RklController;
import org.rustkeylock.fxcomponents.RklStage;
import org.rustkeylock.japi.JavaEntry;
import org.rustkeylock.ui.Defs;
import org.rustkeylock.ui.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ShowEntriesSetCb extends NativeCallbackToRustChannelSupport {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RklStage stage;

    public ShowEntriesSetCb(RklStage rklStage) {
        this.stage = rklStage;
    }

    public void apply(List<JavaEntry> entries, String filter) {
        logger.debug("Callback for showing " + entries.size() + " entries ");
        String processedFilter = filter.equals(Defs.EMPTY_ARG) ? "" : filter;
        Platform.runLater(() -> {
            try {
                URL resurl;
                FXMLLoader loader = new FXMLLoader();

                resurl = getClass().getResource("/fragments/list_entries.fxml");
                loader.setControllerFactory(clazz ->
                        new ListEntriesController(
                                entries,
                                processedFilter)
                );

                loader.setLocation(resurl);
                Parent root = loader.load();

                RklController controller = loader.getController();
                controller.setCallback(this::doCallback);

                Scene scene = new Scene(root);
                Utils.applyRklCss(scene);
                stage.updateView(scene, controller);
            } catch (IOException error) {
                error.printStackTrace();
            }
        });
    }
}
