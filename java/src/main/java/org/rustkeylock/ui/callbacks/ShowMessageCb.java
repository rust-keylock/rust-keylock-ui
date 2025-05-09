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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.rustkeylock.fxcomponents.RklStage;
import org.rustkeylock.japi.JavaUserOption;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

public class ShowMessageCb {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RklStage stage;

    public ShowMessageCb(RklStage rklStage) {
        this.stage = rklStage;
    }

    public CompletableFuture<Object> apply(List<JavaUserOption> options, String message, String severity) {
        String logMessage = String.format("Callback for showing message %s of severity %s and options %s",
                message,
                severity,
                options.stream().map(Object::toString).collect(Collectors.joining(", ")));
        logger.debug(logMessage);
        CompletableFuture<Object> instanceFuture = new CompletableFuture<>();
        Platform.runLater(() -> {
            Alert.AlertType alertType = Alert.AlertType.NONE;
            if ("Info".equals(severity)) {
                alertType = Alert.AlertType.INFORMATION;
            } else if ("Warn".equals(severity)) {
                alertType = Alert.AlertType.WARNING;
            } else if ("Error".equals(severity)) {
                alertType = Alert.AlertType.ERROR;
            }

            List<ButtonType> buttonTypesList = options.stream().map(suo -> new ButtonType(suo.label))
                    .collect(Collectors.toList());
            ButtonType[] buttonTypes = new ButtonType[buttonTypesList.size()];
            for (int i = 0; i < buttonTypesList.size(); i++)
                buttonTypes[i] = buttonTypesList.get(i);

            Alert alert = new Alert(alertType, message, buttonTypes);
            alert.initOwner(stage.getFxStage());
            alert.setTitle("rust-keylock");
            alert.setContentText(message);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            Optional<ButtonType> selectedButtonTypeOption = alert.showAndWait();
            Optional<JavaUserOption> selectedJavaUserOption = selectedButtonTypeOption
                    .flatMap(sbt -> options.stream()
                            .filter(suo -> suo.label.equals(sbt.getText()))
                            .findFirst());

            if (selectedJavaUserOption.isPresent()) {
                instanceFuture.complete(GuiResponse.UserOptionSelected(selectedJavaUserOption.get()));
            } else {
                logger.error("Pressed a button that does not exist in the User Options offered (" +
                        selectedButtonTypeOption.toString() +
                        ")?! How did it get here?? Please consider opening a bug to the developers.");
                instanceFuture.complete(GuiResponse.GoToMenu(JavaMenu.Main()));
            }
        });

        return instanceFuture;
    }
}
