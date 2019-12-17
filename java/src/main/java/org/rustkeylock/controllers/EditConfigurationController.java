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

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.rustkeylock.fxcomponents.RklStage;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;
import org.rustkeylock.ui.Defs;
import org.rustkeylock.ui.callbacks.ShowMenuCb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class EditConfigurationController extends BaseController implements RklController, Initializable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Consumer<Object> callback;
    @FXML
    private JFXTextField ncServerUrl = new JFXTextField();
    @FXML
    private StringProperty ncServerUrlMessage = new SimpleStringProperty("");
    @FXML
    private JFXTextField ncUsername = new JFXTextField();
    @FXML
    private StringProperty ncUsernameMessage = new SimpleStringProperty("");
    @FXML
    private JFXTextField ncPassword = new JFXTextField();
    @FXML
    private StringProperty ncPasswordMessage = new SimpleStringProperty("");
    @FXML
    private JFXCheckBox ncUseSelfSignedCertificate = new JFXCheckBox();
    @FXML
    private StringProperty dropboxTokenLabel = new SimpleStringProperty("");

    private final List<String> strings;
    private final RklStage stage;

    public EditConfigurationController(List<String> strings, RklStage stage) {
        this.strings = strings;
        this.stage = stage;
    }

    @Override
    public void setCallback(Consumer<Object> consumer) {
        this.callback = consumer;
    }

    @Override
    Consumer<Object> getCallback() {
        return this.callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        disableMenuButtons();
        if (strings.size() == 6) {
            ncServerUrl.setText(strings.get(0));
            ncUsername.setText(strings.get(1));
            ncPassword.setText(strings.get(2));
            ncUseSelfSignedCertificate.setSelected(Boolean.parseBoolean(strings.get(3)));

            if (strings.get(5).isEmpty()) {
                setDropboxTokenLabel("Press the button to acquire a new authentication token.");
            } else {
                setDropboxTokenLabel("A token is acquired. Press the button if you want to renew.");
            }
        }
    }

    @FXML
    private void getDropboxToken(ActionEvent event) {
        event.consume();

        try {
            String url = strings.get(4);
            callback.accept(GuiResponse.GoToMenu(JavaMenu.WaitForDbxTokenCallback(url)));
            String envVar = System.getenv("RUST_KEYLOCK_UI_JAVA_USER_HOME");

            if (envVar != null && !envVar.isBlank()) {
                logger.debug("Running in snap! Using xdg-open to open the browser...");
                ProcessBuilder processBuilder = new ProcessBuilder();
                Runtime.getRuntime().exec("xdg-open " + url);
            } else {
                stage.getHostServices().showDocument(url);
            }

            new ShowMenuCb(stage).apply(Defs.MENU_PLEASE_WAIT);

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    @FXML
    private void apply(ActionEvent event) {
        event.consume();

        setNcServerUrlMessage("");

        boolean errorsExist = false;
        if (!ncServerUrl.getText().isBlank() || !ncUsername.getText().isBlank() || !ncPassword.getText().isBlank()) {
            if (ncServerUrl.getText().isBlank()) {
                errorsExist = true;
                setNcServerUrlMessage("Required field");
            } else if (ncUsername.getText().isBlank()) {
                errorsExist = true;
                setNcUsernameMessage("Required field");
            } else if (ncPassword.getText().isBlank()) {
                errorsExist = true;
                setNcPasswordMessage("Required field");
            }
        }

        if (!errorsExist) {
            List<String> newStrings = List.of(
                    ncServerUrl.getText(),
                    ncUsername.getText(),
                    ncPassword.getText(),
                    "" + ncUseSelfSignedCertificate.isSelected(),
                    strings.get(5)
            );

            callback.accept(GuiResponse.SetConfiguration(newStrings));
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        event.consume();

        callback.accept(GuiResponse.GoToMenu(JavaMenu.Main()));
    }

    public JFXTextField getNcServerUrl() {
        return ncServerUrl;
    }

    public void setNcServerUrl(JFXTextField ncServerUrl) {
        this.ncServerUrl = ncServerUrl;
    }

    public String getNcServerUrlMessage() {
        return ncServerUrlMessage.get();
    }

    public StringProperty ncServerUrlMessageProperty() {
        return ncServerUrlMessage;
    }

    public void setNcServerUrlMessage(String ncServerUrlMessage) {
        this.ncServerUrlMessage.set(ncServerUrlMessage);
    }

    public JFXTextField getNcUsername() {
        return ncUsername;
    }

    public void setNcUsername(JFXTextField ncUsername) {
        this.ncUsername = ncUsername;
    }

    public String getNcUsernameMessage() {
        return ncUsernameMessage.get();
    }

    public StringProperty ncUsernameMessageProperty() {
        return ncUsernameMessage;
    }

    public void setNcUsernameMessage(String ncUsernameMessage) {
        this.ncUsernameMessage.set(ncUsernameMessage);
    }

    public JFXTextField getNcPassword() {
        return ncPassword;
    }

    public void setNcPassword(JFXTextField ncPassword) {
        this.ncPassword = ncPassword;
    }

    public String getNcPasswordMessage() {
        return ncPasswordMessage.get();
    }

    public StringProperty ncPasswordMessageProperty() {
        return ncPasswordMessage;
    }

    public void setNcPasswordMessage(String ncPasswordMessage) {
        this.ncPasswordMessage.set(ncPasswordMessage);
    }

    public JFXCheckBox getNcUseSelfSignedCertificate() {
        return ncUseSelfSignedCertificate;
    }

    public void setNcUseSelfSignedCertificate(JFXCheckBox ncUseSelfSignedCertificate) {
        this.ncUseSelfSignedCertificate = ncUseSelfSignedCertificate;
    }

    public String getDropboxTokenLabel() {
        return dropboxTokenLabel.get();
    }

    public StringProperty dropboxTokenLabelProperty() {
        return dropboxTokenLabel;
    }

    public void setDropboxTokenLabel(String dropboxTokenLabel) {
        this.dropboxTokenLabel.set(dropboxTokenLabel);
    }

}
