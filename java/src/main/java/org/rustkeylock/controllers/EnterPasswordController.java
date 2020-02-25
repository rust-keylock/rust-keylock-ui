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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import org.rustkeylock.fxcomponents.RklStage;
import org.rustkeylock.japi.stubs.GuiResponse;

import java.util.function.Consumer;

public class EnterPasswordController implements RklController {
    @FXML
    private PasswordField password = new PasswordField();
    @FXML
    private StringProperty passwordMessage = new SimpleStringProperty("");
    @FXML
    private PasswordField number = new PasswordField();
    @FXML
    private StringProperty numberMessage = new SimpleStringProperty("");
    private Consumer<Object> callback;
    private final RklStage stage;

    public EnterPasswordController(RklStage stage) {
        this.stage = stage;
    }

    @FXML
    private void decrypt(ActionEvent event) {
        event.consume();
        setPasswordMessage("");
        setNumberMessage("");

        if (getPassword().getText().trim().isEmpty()) {
            setPasswordMessage("Required Field");
        } else if (getNumber().getText().trim().isEmpty()) {
            setNumberMessage("Required Field");
        } else {
            try {
                Integer num = Integer.parseInt(getNumber().getText().trim());
                callback.accept(GuiResponse.ChangePassword(getPassword().getText().trim(), num));
                if (stage != null) {
                    stage.markLoggedIn();
                }
            } catch (Exception error) {
                String message = "Incorrect number";
                error.printStackTrace();
                getNumber().setText("");
                setNumberMessage(message);
            }
        }
    }

    @Override
    public void setCallback(Consumer<Object> consumer) {
        callback = consumer;
    }

    public String getPasswordMessage() {
        return passwordMessage.get();
    }

    public StringProperty passwordMessageProperty() {
        return passwordMessage;
    }

    public void setPasswordMessage(String passwordMessage) {
        this.passwordMessage.set(passwordMessage);
    }

    public String getNumberMessage() {
        return numberMessage.get();
    }

    public StringProperty numberMessageProperty() {
        return numberMessage;
    }

    public void setNumberMessage(String numberMessage) {
        this.numberMessage.set(numberMessage);
    }

    public PasswordField getPassword() {
        return password;
    }

    public void setPassword(PasswordField password) {
        this.password = password;
    }

    public PasswordField getNumber() {
        return number;
    }

    public void setNumber(PasswordField number) {
        this.number = number;
    }
}
