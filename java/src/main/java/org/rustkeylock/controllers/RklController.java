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

public interface RklController {
    /**
     * When the User selects something that needs to be passed to native rust-keylock lib handling, 
     * the CompletableFuture will be completed and provide the user selection as a J4rs Instance
     * 
     * @return A CompletableFuture
     */
    CompletableFuture<Object> getResponseFuture();

    /**
     * Creates an Instance from the passed Object and submits it as response to the providced CompletableFuture
     * @param response The Object to create the j4rs Instance
     */
    default void submitResponse(Object response) {
        getResponseFuture().complete(response);
    }

    void createNewResponseFuture();
}
