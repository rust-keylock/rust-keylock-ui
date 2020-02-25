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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import org.rustkeylock.fxcomponents.RklStage;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.ui.Defs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ChangePasswordController extends BaseController implements RklController, Initializable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @FXML
    private PasswordField password1 = new PasswordField();
    @FXML
    private StringProperty passwordMessage1 = new SimpleStringProperty("");
    @FXML
    private PasswordField password2 = new PasswordField();
    @FXML
    private StringProperty passwordMessage2 = new SimpleStringProperty("");
    @FXML
    private PasswordField number1 = new PasswordField();
    @FXML
    private StringProperty numberMessage1 = new SimpleStringProperty("");
    @FXML
    private PasswordField number2 = new PasswordField();
    @FXML
    private StringProperty numberMessage2 = new SimpleStringProperty("");
    @FXML
    private BooleanProperty cancelButtonVisible = new SimpleBooleanProperty(true);
    private Consumer<Object> callback;
    private final RklStage stage;

    public ChangePasswordController(RklStage stage) {
        this.stage = stage;
    }

    @FXML
    private void applyNewPassword(ActionEvent event) {
        event.consume();

        setPasswordMessage1("");
        setPasswordMessage2("");
        setNumberMessage1("");
        setNumberMessage2("");

        if (!password1.getText().equals(password2.getText())) {
            setPasswordMessage2("The provided passwords did not match");
        } else if (!number1.getText().equals(number2.getText())) {
            setNumberMessage2("The provided favorite numbers did not match");
        } else {
            if (password1.getText().trim().isEmpty()) {
                setPasswordMessage1("This Field cannot be empty");
                password1.clear();
            } else if (number1.getText().trim().isEmpty()) {
                setNumberMessage1("This Field cannot be empty");
                number1.clear();
            } else {
                try {
                    logger.info("Password and number changed");
                    callback.accept(GuiResponse.ChangePassword(password1.getText().trim(), Integer.parseInt(number1.getText().trim())));
                    stage.markLoggedIn();
                } catch (Exception error) {
                    String message = "Incorrect number";
                    error.printStackTrace();
                    number1.clear();
                    setNumberMessage1(message);
                }
            }
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        event.consume();

        callback.accept(GuiResponse.GoToMenu(Defs.MENU_MAIN));
    }

    @Override
    public void setCallback(Consumer<Object> consumer) {
        callback = consumer;
    }

    @Override
    Consumer<Object> getCallback() {
        return this.callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (stage.isLoggedIn()) {
            disableMenuButtons();
        } else {
            hideMenuButtons();
            setCancelButtonVisible(false);
        }
    }

    public PasswordField getPassword1() {
        return password1;
    }

    public void setPassword1(PasswordField password1) {
        this.password1 = password1;
    }

    public String getPasswordMessage1() {
        return passwordMessage1.get();
    }

    public StringProperty passwordMessage1Property() {
        return passwordMessage1;
    }

    public void setPasswordMessage1(String passwordMessage1) {
        this.passwordMessage1.set(passwordMessage1);
    }

    public PasswordField getPassword2() {
        return password2;
    }

    public void setPassword2(PasswordField password2) {
        this.password2 = password2;
    }

    public String getPasswordMessage2() {
        return passwordMessage2.get();
    }

    public StringProperty passwordMessage2Property() {
        return passwordMessage2;
    }

    public void setPasswordMessage2(String passwordMessage2) {
        this.passwordMessage2.set(passwordMessage2);
    }

    public PasswordField getNumber1() {
        return number1;
    }

    public void setNumber1(PasswordField number1) {
        this.number1 = number1;
    }

    public String getNumberMessage1() {
        return numberMessage1.get();
    }

    public StringProperty numberMessage1Property() {
        return numberMessage1;
    }

    public void setNumberMessage1(String numberMessage1) {
        this.numberMessage1.set(numberMessage1);
    }

    public PasswordField getNumber2() {
        return number2;
    }

    public void setNumber2(PasswordField number2) {
        this.number2 = number2;
    }

    public String getNumberMessage2() {
        return numberMessage2.get();
    }

    public StringProperty numberMessage2Property() {
        return numberMessage2;
    }

    public void setNumberMessage2(String numberMessage2) {
        this.numberMessage2.set(numberMessage2);
    }

    public boolean isCancelButtonVisible() {
        return cancelButtonVisible.get();
    }

    public BooleanProperty cancelButtonVisibleProperty() {
        return cancelButtonVisible;
    }

    public void setCancelButtonVisible(boolean cancelButtonVisible) {
        this.cancelButtonVisible.set(cancelButtonVisible);
    }
}
