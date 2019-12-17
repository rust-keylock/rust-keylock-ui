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
package org.rustkeylock.ui;

import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.astonbitecode.j4rs.api.invocation.NativeCallbackToRustChannelSupport;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;

public class UiStopper extends NativeCallbackToRustChannelSupport implements EventHandler<WindowEvent> {
    public void stop() {
        doCallback(GuiResponse.GoToMenu(JavaMenu.Exit()));
    }

    @Override
    public void handle(WindowEvent ev) {
        stop();
        ev.consume();
    }
}
