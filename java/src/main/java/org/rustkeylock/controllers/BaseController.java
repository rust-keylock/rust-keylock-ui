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

import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public abstract class BaseController implements RklController {
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
        event.consume();
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.Exit()));
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
