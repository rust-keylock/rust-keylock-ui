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

import java.util.concurrent.CompletableFuture;

import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ExitController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private CompletableFuture<Object> responseFuture = new CompletableFuture<>();

    @FXML
    private void yesAction(ActionEvent event) {
        event.consume();

        logger.debug("The User selected to force Exit with unsaved data");
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.ForceExit()));
    }

    @FXML
    private void noAction(ActionEvent event) {
        event.consume();

        logger.debug("The User selected not to exit because of unsaved data");
        this.submitResponse(GuiResponse.GoToMenu(JavaMenu.Main()));
    }

    @Override
    public CompletableFuture<Object> getResponseFuture() {
        return responseFuture;
    }

    @Override
    public void createNewResponseFuture() {
        responseFuture = new CompletableFuture<>();
    }
}
