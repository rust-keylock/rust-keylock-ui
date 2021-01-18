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

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.rustkeylock.japi.JavaEntry;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.rustkeylock.japi.stubs.JavaMenu;
import org.rustkeylock.ui.Defs;
import org.rustkeylock.ui.UiLauncher;
import org.rustkeylock.ui.callbacks.ShowMenuCb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ListEntriesController extends BaseController implements RklController, Initializable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Consumer<Object> callback;
    @FXML
    private TextField filterTextField = new TextField();
    private String initialFilter;
    @FXML
    private ListView<JavaEntry> entriesListView;
    private List<JavaEntry> entries;

    public ListEntriesController(List<JavaEntry> entries, String initialFilter) {
        this.entriesListView = new ListView<>();
        this.entries = entries;
        this.initialFilter = initialFilter;
    }

    @FXML
    private void checkPasswordsHealth(ActionEvent event) {
        event.consume();
        new ShowMenuCb(UiLauncher.getStage()).apply(Defs.MENU_PLEASE_WAIT);
        callback.accept(GuiResponse.CheckPasswords());
    }

    @FXML
    private void addEntry(ActionEvent event) {
        event.consume();
        callback.accept(GuiResponse.GoToMenu(JavaMenu.NewEntry()));
    }

    @FXML
    private void doFilter(ActionEvent event) {
        event.consume();
        applyFilter();
    }

    @FXML
    public void entryClicked(MouseEvent event) {
        event.consume();
        int pos = this.entriesListView.getSelectionModel().getSelectedIndex();
        if (pos >= 0 && pos < entries.size()) {
            logger.debug("Clicked entry with index " + pos + " in the list of entries");
            callback.accept(GuiResponse.GoToMenu(JavaMenu.ShowEntry(pos)));
        }
    }

    @FXML
    public void filterChanged(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            applyFilter();
        } else if (event.getCode().equals(KeyCode.ESCAPE)) {
            filterTextField.setText("");
            applyFilter();
        }
    }

    private void applyFilter() {
        logger.debug("Filter changed to '" + filterTextField.getText() + "'");
        callback.accept(GuiResponse.GoToMenu(JavaMenu.EntriesList(filterTextField.getText())));
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
        this.entriesListView.setCellFactory(cf -> new EntryListCell());
        this.entriesListView.setItems(FXCollections.observableArrayList(entries));
        filterTextField.setText(initialFilter);
    }

    public ListView<JavaEntry> getEntriesListView() {
        return entriesListView;
    }

    public void setEntriesListView(ListView<JavaEntry> entriesListView) {
        this.entriesListView = entriesListView;
    }

    public TextField getFilterTextField() {
        return filterTextField;
    }

    public void setFilterTextField(TextField filterTextField) {
        this.filterTextField = filterTextField;
    }

    private class EntryListCell extends ListCell<JavaEntry> {
        @Override
        protected void updateItem(JavaEntry item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                HBox b = new HBox();
                b.setAlignment(Pos.CENTER_LEFT);
                Label label = new Label(item.getName());
                label.setAlignment(Pos.CENTER_LEFT);
                if (item.getMeta().isLeakedpassword()) {
                    setStyle(Defs.BACKGROUND_ERROR);
                    setTooltip(new Tooltip("This password is leaked!"));
                } else {
                    setStyle(Defs.BACKGROUND_NO_ERROR);
                    setTooltip(null);
                }
                b.getChildren().add(label);
                setGraphic(b);
            }
        }
    }
}
