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

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.rustkeylock.japi.stubs.GuiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ImportExportController extends BaseController implements RklController, Initializable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @FXML
    private StringProperty title = new SimpleStringProperty("");
    @FXML
    private JFXTextField path = new JFXTextField();
    @FXML
    private StringProperty pathMessage = new SimpleStringProperty("");
    @FXML
    private JFXPasswordField password = new JFXPasswordField();
    @FXML
    private StringProperty passwordMessage = new SimpleStringProperty("");
    @FXML
    private JFXPasswordField number = new JFXPasswordField();
    @FXML
    private StringProperty numberMessage = new SimpleStringProperty("");
    private Consumer<Object> callback;
    private final Stage stage;
    private BooleanProperty export = new SimpleBooleanProperty(false);
    private String homePath = System.getProperty("user.home");
    private String proposedFilename = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + "_rust_keylock";
    private String pathString = homePath + File.separator + proposedFilename;

    public ImportExportController(boolean export, Stage stage) {
        setExport(export);
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        path.setText(pathString);
        if (isExport()) {
            setTitle("Where to export?");
            password.setVisible(false);
            number.setVisible(false);
        } else {
            setTitle("What to import?");
        }
    }

    @FXML
    private void apply(ActionEvent event) {
        event.consume();

        setPathMessage("");
        setPasswordMessage("");
        setNumberMessage("");

        pathString = path.getText();
        if (isExport()) {
            logger.info("Exporting to " + pathString);

            if (pathString.isEmpty()) {
                setPathMessage("Required Field");
            } else if (new File(pathString).isDirectory()) {
                setPathMessage("Cannot export to a directory");
            } else {
                callback.accept(GuiResponse.ExportImport(pathString, 1, "Dummy", 11));
            }
        } else {
            if (pathString.isEmpty()) {
                logger.info("Importing from " + path);
                setPathMessage("Required Field");
            } else if (new File(pathString).isDirectory()) {
                setPathMessage("Cannot import from a directory");
            } else if (password.getText().isEmpty()) {
                setPasswordMessage("Required Field");
            } else if (number.getText().isEmpty()) {
                setNumberMessage("Required Field");
            } else {
                try {
                    Integer numberInt = Integer.parseInt(number.getText());
                    callback.accept(GuiResponse.ExportImport(pathString, 0, password.getText(), numberInt));
                } catch (Exception error) {
                    String message = "Incorrect number";
                    error.printStackTrace();
                    number.clear();
                    setNumberMessage(message);
                }
            }
        }
    }

    @FXML
    private void browseForFile(ActionEvent event) {
        event.consume();

        setPathMessage("");
        setPasswordMessage("");
        setNumberMessage("");

        if (isExport()) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(homePath));
            File file = directoryChooser.showDialog(stage);
            if (file != null) {
                pathString = file.getAbsolutePath() + File.separator + proposedFilename;
            }
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(homePath));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                pathString = file.getAbsolutePath();
            }
        }

        logger.info("Chosen file: " + path);
        path.setText(pathString);
    }

    @Override
    public void setCallback(Consumer<Object> consumer) {
        callback = consumer;
    }

    @Override
    Consumer<Object> getCallback() {
        return this.callback;
    }

    public JFXTextField getPath() {
        return path;
    }

    public void setPath(JFXTextField path) {
        this.path = path;
    }

    public String getPathMessage() {
        return pathMessage.get();
    }

    public StringProperty pathMessageProperty() {
        return pathMessage;
    }

    public void setPathMessage(String pathMessage) {
        this.pathMessage.set(pathMessage);
    }

    public JFXPasswordField getPassword() {
        return password;
    }

    public void setPassword(JFXPasswordField password) {
        this.password = password;
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

    public JFXPasswordField getNumber() {
        return number;
    }

    public void setNumber(JFXPasswordField number) {
        this.number = number;
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

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public boolean isExport() {
        return export.get();
    }

    public BooleanProperty exportProperty() {
        return export;
    }

    public void setExport(boolean export) {
        this.export.set(export);
    }
}
