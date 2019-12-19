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

import javafx.scene.Node;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class Utils {
    public static void removeChildNodeById(HBox parent, String idToRemove) {
        Optional<Node> childFound = parent.getChildren().stream()
                .filter(node -> idToRemove.equals(node.getId()))
                .findFirst();
        childFound.map(node -> parent.getChildren().remove(node));
    }

    public static void addNode(HBox parent, Node node) {
        parent.getChildren().add(node);
    }
}
