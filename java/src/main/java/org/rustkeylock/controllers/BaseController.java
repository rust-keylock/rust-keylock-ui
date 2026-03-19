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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock. If not, see <http://www.gnu.org/licenses/>.
package org.rustkeylock.controllers;

import javax.swing.ImageIcon;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;
import org.rustkeylock.ui.UiLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public abstract class BaseController implements RklController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @FXML
    private Button goToEntriesButton;
    @FXML
    private Button encryptAndSaveButton;
    @FXML
    private Button goToChangePassButton;
    @FXML
    private Button goToExportToButton;
    @FXML
    private Button goToImportFromButton;
    @FXML
    private Button goToShowConfigurationButton;
    @FXML
    private Button minimizeToTrayButton;
    @FXML
    private Button goToExitButton;

    @FXML
    private void goToShowEntries(ActionEvent event) {
        event.consume();
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.EntriesList(null)));
    }

    @FXML
    private void encryptAndSave(ActionEvent event) {
        event.consume();
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.Save(false)));
    }

    @FXML
    private void goToChangePassword(ActionEvent event) {
        event.consume();
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.ChangePass()));
    }

    @FXML
    private void goToExportTo(ActionEvent event) {
        event.consume();
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.ExportEntries()));
    }

    @FXML
    private void goToImportFrom(ActionEvent event) {
        event.consume();
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.ImportEntries()));
    }

    @FXML
    private void goToShowConfiguration(ActionEvent event) {
        event.consume();
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.ShowConfiguration()));
    }

    @FXML
    private void goToExit(ActionEvent event) {
        if (event != null) {
            event.consume();
        }
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.Exit()));
    }

    @FXML
    private void minimizeToTray(ActionEvent event) {
        event.consume();
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
    }

    protected void disableMenuButtons() {
        toggleButtons(true);
    }

    protected void enableMenuButtons() {
        toggleButtons(false);
    }

    protected void hideMenuButtons() {
        goToEntriesButton.setVisible(false);
        encryptAndSaveButton.setVisible(false);
        goToChangePassButton.setVisible(false);
        minimizeToTrayButton.setVisible(false);
        goToExitButton.setVisible(false);
        goToExportToButton.setVisible(false);
        goToImportFromButton.setVisible(false);
        goToShowConfigurationButton.setVisible(false);
    }

    private void toggleButtons(boolean toggle) {
        goToEntriesButton.setDisable(toggle);
        encryptAndSaveButton.setDisable(toggle);
        goToChangePassButton.setDisable(toggle);
        minimizeToTrayButton.setDisable(toggle);
        goToExitButton.setDisable(toggle);
        goToExportToButton.setDisable(toggle);
        goToImportFromButton.setDisable(toggle);
        goToShowConfigurationButton.setDisable(toggle);
    }

    private void addAppToTray() {
        logger.debug("Minimizing to tray");
        try {
            java.awt.Toolkit.getDefaultToolkit();

            if (!java.awt.SystemTray.isSupported()) {
                logger.error("System tray is not supported");
                this.submitResponse(GuiResponse.GoToMenu(JavaMenu.ShowConfiguration()));
            }

            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            java.awt.Image image = new ImageIcon(this.getClass().getResource("/images/rkl-16.png")).getImage();
            java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);

            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

            java.awt.MenuItem openItem = new java.awt.MenuItem("Open rust-keylock-ui");
            openItem.addActionListener(event -> Platform.runLater(this::showStage));

            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                goToExit(null);
                tray.remove(trayIcon);
            });

            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            tray.add(trayIcon);
            Platform.runLater(this::hideStage);
        } catch (java.awt.AWTException error) {
            logger.error("Unable to init system tray", error);
        }
    }

    private void showStage() {
        if (UiLauncher.getStage() != null && UiLauncher.getStage().getFxStage() != null) {
            Platform.setImplicitExit(true);
            Stage stage = UiLauncher.getStage().getFxStage();
            stage.show();
            stage.toFront();
        }
    }

    private void hideStage() {
        if (UiLauncher.getStage() != null && UiLauncher.getStage().getFxStage() != null) {
            Platform.setImplicitExit(false);
            Stage stage = UiLauncher.getStage().getFxStage();
            stage.hide();
        }
    }

}
