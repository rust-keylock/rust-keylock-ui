// Copyright 2017 astonbitecode
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
package org.rustkeylock.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.rustkeylock.fxcomponents.RklStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class UiLauncher extends Application {
    private static final Logger logger = LoggerFactory.getLogger(UiLauncher.class);
    // java --module-path $RUST_KEYLOCK_UI_BASE_PATH/java/target/lib --add-modules javafx.base,javafx.controls,javafx.graphics,javafx.fxml  -jar $RUST_KEYLOCK_UI_BASE_PATH/java/target/rust-keylock-ui-java-0.12.0.jar
    private static AtomicReference<Optional<RklStage>> stageOpt = new AtomicReference<>(Optional.empty());
    private UiStopper stopper = new UiStopper();

    public static void launch() {
        new Thread(() -> Application.launch()).start();
    }

    @Override
    public void start(Stage fxStage) throws Exception {
        logger.info("Starting rust-keylock-ui");
        fxStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/rkl.png")));
        URL resurl = getClass().getResource("/fragments/logo.fxml");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resurl);
        Parent root = loader.load();

        Scene scene = new Scene(root);

        fxStage.setScene(scene);
        fxStage.setTitle("rust-keylock");
        fxStage.show();
        stageOpt.getAndSet(Optional.of(new RklStage(fxStage, getHostServices())));
    }

    public static RklStage getStage() {
        if (stageOpt.get().isEmpty()) {
            try {
                logger.debug("Stage is not available yet. Waiting...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getStage();
        } else {
            return stageOpt.get().get();
        }
    }

    // This is called from the native in order to activate asynchronous callback for the exit event
    public static UiStopper initOnCloseHandler() {
        UiStopper stopper = new UiStopper();
        getStage().getFxStage().setOnCloseRequest(stopper);
        return stopper;
    }

}
