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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ExitController extends BaseController implements RklController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Consumer<Object> callback;

    @FXML
    private void yesAction(ActionEvent event) {
        event.consume();

        logger.debug("The User selected to force Exit with unsaved data");
        callback.accept(GuiResponse.GoToMenu(JavaMenu.ForceExit()));
    }

    @FXML
    private void noAction(ActionEvent event) {
        event.consume();

        logger.debug("The User selected not to exit because of unsaved data");
        callback.accept(GuiResponse.GoToMenu(JavaMenu.Main()));
    }

    @Override
    Consumer<Object> getCallback() {
        return this.callback;
    }

    @Override
    public void setCallback(Consumer<Object> consumer) {
        callback = consumer;
    }
}
