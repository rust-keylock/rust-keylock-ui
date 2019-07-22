// Copyright 2017 astonbitecode
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

use std::sync::mpsc::{self, Receiver};

use j4rs::{Instance, InvocationArg, Jvm};
use log::*;
use rust_keylock::{AsyncEditor, Entry, Menu, MessageSeverity, RklConfiguration, Safe, UserOption, UserSelection};
use rust_keylock::dropbox::DropboxConfiguration;
use serde_derive::{Deserialize, Serialize};

pub struct DesktopImpl {
    jvm: Jvm,
    launcher: Instance,
    show_menu: Instance,
    show_entries: Instance,
    show_entry: Instance,
    edit_configuration: Instance,
    show_message: Instance,
}

pub fn new(jvm: Jvm) -> DesktopImpl {
    // Start the Ui
    debug!("Calling org.rustkeylock.japi.Launcher.start");
    let launcher = jvm.invoke_static(
        "org.rustkeylock.japi.Launcher",
        "start",
        &Vec::new())
        .unwrap();
    debug!("Calling org.rustkeylock.japi.Launcher.getStage");
    let fx_stage = jvm.invoke_static("org.rustkeylock.japi.Launcher", "getStage", &Vec::new()).unwrap();
    debug!("Stage retrieved. Proceeding...");
    // Create the Java classes that navigate the UI
    let show_menu = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowMenuCb",
        &[InvocationArg::from(jvm.clone_instance(&fx_stage).unwrap())]).unwrap();
    let show_entries = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowEntriesSetCb",
        &[InvocationArg::from(jvm.clone_instance(&fx_stage).unwrap())]).unwrap();
    let show_entry = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowEntryCb",
        &[InvocationArg::from(jvm.clone_instance(&fx_stage).unwrap())]).unwrap();
    let edit_configuration = jvm.create_instance(
        "org.rustkeylock.callbacks.EditConfigurationCb",
        &[InvocationArg::from(jvm.clone_instance(&fx_stage).unwrap())]).unwrap();
    let show_message = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowMessageCb",
        &[InvocationArg::from(jvm.invoke_static("org.rustkeylock.japi.Launcher", "getStage", &Vec::new()).unwrap())]).unwrap();
    // Return the Editor
    DesktopImpl {
        jvm,
        launcher,
        show_menu,
        show_entries,
        show_entry,
        edit_configuration,
        show_message,
    }
}

impl AsyncEditor for DesktopImpl {
    fn show_password_enter(&self) -> Receiver<UserSelection> {
        debug!("Opening the password fragment");
        let instance_receiver = self.jvm.invoke_to_channel(
            &self.show_menu,
            "apply",
            &[InvocationArg::from("TryPass")]);
        debug!("Waiting for password...");
        super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver, &self.launcher)
    }

    fn show_change_password(&self) -> Receiver<UserSelection> {
        debug!("Opening the change password fragment");
        let instance_receiver = self.jvm.invoke_to_channel(
            &self.show_menu,
            "apply",
            &[InvocationArg::from("ChangePass")]);
        debug!("Waiting for password...");
        super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver, &self.launcher)
    }

    fn show_menu(&self, menu: &Menu, safe: &Safe, configuration: &RklConfiguration) -> Receiver<UserSelection> {
        debug!("Opening menu '{:?}' with entries size {}", menu, safe.get_entries().len());

        let instance_receiver_res = match menu {
            &Menu::Main => {
                self.jvm.invoke_to_channel(
                    &self.show_menu,
                    "apply",
                    &[InvocationArg::from("Main")])
            }
            &Menu::EntriesList(_) => {
                let scala_entries: Vec<ScalaEntry> = safe.get_entries().iter()
                    .map(|entry| ScalaEntry::new(entry))
                    .collect();
                let filter = if safe.get_filter().is_empty() {
                    "null".to_string()
                } else {
                    safe.get_filter().clone()
                };
                self.jvm.invoke_to_channel(
                    &self.show_entries,
                    "apply",
                    &[
                        InvocationArg::from((
                            scala_entries.as_slice(),
                            "org.rustkeylock.japi.ScalaEntry",
                            &self.jvm)),
                        InvocationArg::from(filter)])
            }
            &Menu::ShowEntry(index) => {
                let entry = safe.get_entry_decrypted(index);
                self.jvm.invoke_to_channel(
                    &self.show_entry,
                    "apply",
                    &[
                        InvocationArg::new(&ScalaEntry::new(&entry), "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(false),
                        InvocationArg::from(false)
                    ])
            }
            &Menu::DeleteEntry(index) => {
                let entry = ScalaEntry::new(safe.get_entry(index));

                self.jvm.invoke_to_channel(
                    &self.show_entry,
                    "apply",
                    &[
                        InvocationArg::new(&entry, "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(false),
                        InvocationArg::from(true)
                    ])
            }
            &Menu::NewEntry => {
                let empty_entry = ScalaEntry::empty();
                // In order to denote that this is a new entry, put -1 as index
                self.jvm.invoke_to_channel(
                    &self.show_entry,
                    "apply",
                    &[
                        InvocationArg::new(&empty_entry, "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(-1),
                        InvocationArg::from(true),
                        InvocationArg::from(false)
                    ])
            }
            &Menu::EditEntry(index) => {
                let selected_entry = safe.get_entry_decrypted(index);
                self.jvm.invoke_to_channel(
                    &self.show_entry,
                    "apply",
                    &[
                        InvocationArg::new(&ScalaEntry::new(&selected_entry), "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(true),
                        InvocationArg::from(false)
                    ])
            }
            &Menu::ExportEntries => {
                self.jvm.invoke_to_channel(
                    &self.show_menu,
                    "apply",
                    &[InvocationArg::from("ExportEntries")])
            }
            &Menu::ImportEntries => {
                self.jvm.invoke_to_channel(
                    &self.show_menu,
                    "apply",
                    &[InvocationArg::from("ImportEntries")])
            }
            &Menu::ShowConfiguration => {
                let conf_strings = vec![
                    configuration.nextcloud.server_url.clone(),
                    configuration.nextcloud.username.clone(),
                    configuration.nextcloud.decrypted_password().unwrap(),
                    configuration.nextcloud.use_self_signed_certificate.to_string(),
                    DropboxConfiguration::dropbox_url(),
                    configuration.dropbox.decrypted_token().unwrap()];
                self.jvm.invoke_to_channel(
                    &self.edit_configuration,
                    "apply",
                    &[InvocationArg::from((conf_strings.as_slice(), &self.jvm))])
            }
            &Menu::Current => {
                self.jvm.invoke_to_channel(
                    &self.show_menu,
                    "apply",
                    &[InvocationArg::from("Current")])
            }
            other => panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug to the developers.", other),
        };

        super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver_res, &self.launcher)
    }

    fn exit(&self, contents_changed: bool) -> Receiver<UserSelection> {
        debug!("Exiting rust-keylock ui...");
        if contents_changed {
            let instance_receiver = self.jvm.invoke_to_channel(
                &self.show_menu,
                "apply",
                &[InvocationArg::from("Exit")]);

            super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver, &self.launcher)
        } else {
            let (tx, rx) = mpsc::channel();
            let _ = tx.send(UserSelection::GoTo(Menu::ForceExit));
            rx
        }
    }

    fn show_message(&self, message: &str, options: Vec<UserOption>, severity: MessageSeverity) -> Receiver<UserSelection> {
        debug!("Showing Message '{}'", message);
        let scala_user_options: Vec<ScalaUserOption> = options.iter()
            .clone()
            .map(|user_option| ScalaUserOption::new(user_option))
            .collect();
        let instance_receiver = self.jvm.invoke_to_channel(
            &self.show_message,
            "apply",
            &[
                InvocationArg::from((
                    scala_user_options.as_slice(),
                    "org.rustkeylock.japi.ScalaUserOption",
                    &self.jvm)),
                InvocationArg::from(message),
                InvocationArg::from(severity.to_string())]);

        super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver, &self.launcher)
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub(crate) struct ScalaEntry {
    pub name: String,
    pub url: String,
    pub user: String,
    pub pass: String,
    pub desc: String,
}

impl ScalaEntry {
    fn new(entry: &Entry) -> ScalaEntry {
        ScalaEntry {
            name: entry.name.clone(),
            url: entry.url.clone(),
            user: entry.user.clone(),
            pass: entry.pass.clone(),
            desc: entry.desc.clone(),
        }
    }

    fn empty() -> ScalaEntry {
        ScalaEntry {
            name: "".to_string(),
            url: "".to_string(),
            user: "".to_string(),
            pass: "".to_string(),
            desc: "".to_string(),
        }
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub(crate) struct ScalaUserOption {
    pub label: String,
    pub value: String,
    pub short_label: String,
}

impl ScalaUserOption {
    fn new(user_option: &UserOption) -> ScalaUserOption {
        ScalaUserOption {
            label: user_option.label.clone(),
            value: user_option.value.to_string(),
            short_label: user_option.short_label.clone(),
        }
    }
}

#[derive(Deserialize, Serialize, Debug)]
pub(crate) enum ScalaMenu {
    TryPass { b: bool },
    ChangePass,
    Main,
    EntriesList { filter: String },
    NewEntry,
    ShowEntry { idx: usize },
    EditEntry { idx: usize },
    DeleteEntry { idx: usize },
    Save { b: bool },
    Exit,
    ForceExit,
    TryFileRecovery,
    ImportEntries,
    ExportEntries,
    ShowConfiguration,
    WaitForDbxTokenCallback { s: String },
    SetDbxToken { s: String },
    Current,
}

impl ScalaMenu {
    pub(crate) fn to_menu(self) -> Menu {
        match self {
            ScalaMenu::Main => Menu::Main,
            ScalaMenu::Exit => Menu::Exit,
            ScalaMenu::EntriesList { filter } => Menu::EntriesList(filter),
            ScalaMenu::Save { b } => Menu::Save(b),
            ScalaMenu::ChangePass => Menu::ChangePass,
            ScalaMenu::ExportEntries => Menu::ExportEntries,
            ScalaMenu::ImportEntries => Menu::ImportEntries,
            ScalaMenu::ShowConfiguration => Menu::ShowConfiguration,
            ScalaMenu::ForceExit => Menu::ForceExit,
            ScalaMenu::NewEntry => Menu::NewEntry,
            ScalaMenu::WaitForDbxTokenCallback { s } => Menu::WaitForDbxTokenCallback(s),
            ScalaMenu::ShowEntry { idx } => Menu::ShowEntry(idx),
            ScalaMenu::EditEntry { idx } => Menu::EditEntry(idx),
            ScalaMenu::DeleteEntry { idx } => Menu::DeleteEntry(idx),
            _ => {
                Menu::Current
            }
        }
    }
}