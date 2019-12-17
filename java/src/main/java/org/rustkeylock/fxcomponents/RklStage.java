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
package org.rustkeylock.fxcomponents;

import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.rustkeylock.controllers.RklController;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class RklStage {
    private final Stage fxStage;
    private final HostServices hostServices;
    private RklController currentlyActiveController;
    private AtomicBoolean loggedIn = new AtomicBoolean(false);

    public RklStage(Stage fxStage, HostServices hostServices) {
        this.fxStage = fxStage;
        this.hostServices = hostServices;
    }

    public Optional<RklController> getCurrentlyActiveController() {
        return Optional.ofNullable(currentlyActiveController);
    }

    public void updateView(Scene scene, RklController controller) {
        currentlyActiveController = controller;
        fxStage.setScene(scene);
    }

    public Stage getFxStage() {
        return this.fxStage;
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    public void markLoggedIn() {
        loggedIn.getAndSet(true);
    }

    public boolean isLoggedIn() {
        return loggedIn.get();
    }
}
