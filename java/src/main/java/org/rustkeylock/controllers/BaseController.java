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
package org.rustkeylock.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;

import java.util.function.Consumer;

public abstract class BaseController {
    @FXML
    private JFXButton goToEntriesButton;
    @FXML
    private JFXButton encryptAndSaveButton;
    @FXML
    private JFXButton goToChangePassButton;
    @FXML
    private JFXButton goToExportToButton;
    @FXML
    private JFXButton goToImportFromButton;
    @FXML
    private JFXButton goToShowConfigurationButton;
    @FXML
    private JFXButton goToExitButton;

    abstract Consumer<Object> getCallback();

    @FXML
    private void goToShowEntries(ActionEvent event) {
        event.consume();
        getCallback().accept(GuiResponse.GoToMenu(JavaMenu.EntriesList("")));
    }

    @FXML
    private void encryptAndSave(ActionEvent event) {
        event.consume();
        getCallback().accept(GuiResponse.GoToMenu(JavaMenu.Save(false)));
    }

    @FXML
    private void goToChangePassword(ActionEvent event) {
        event.consume();
        getCallback().accept(GuiResponse.GoToMenu(JavaMenu.ChangePass()));
    }

    @FXML
    private void goToExportTo(ActionEvent event) {
        event.consume();
        getCallback().accept(GuiResponse.GoToMenu(JavaMenu.ExportEntries()));
    }

    @FXML
    private void goToImportFrom(ActionEvent event) {
        event.consume();
        getCallback().accept(GuiResponse.GoToMenu(JavaMenu.ImportEntries()));
    }

    @FXML
    private void goToShowConfiguration(ActionEvent event) {
        event.consume();
        getCallback().accept(GuiResponse.GoToMenu(JavaMenu.ShowConfiguration()));
    }

    @FXML
    private void goToExit(ActionEvent event) {
        event.consume();
        getCallback().accept(GuiResponse.GoToMenu(JavaMenu.Exit()));
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
        goToExitButton.setVisible(false);
        goToExportToButton.setVisible(false);
        goToImportFromButton.setVisible(false);
        goToShowConfigurationButton.setVisible(false);
    }

    private void toggleButtons(boolean toggle) {
        goToEntriesButton.setDisable(toggle);
        encryptAndSaveButton.setDisable(toggle);
        goToChangePassButton.setDisable(toggle);
        goToExitButton.setDisable(toggle);
        goToExportToButton.setDisable(toggle);
        goToImportFromButton.setDisable(toggle);
        goToShowConfigurationButton.setDisable(toggle);
    }
}
