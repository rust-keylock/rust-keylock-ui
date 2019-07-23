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
use serde_derive::{Deserialize, Serialize};

use rust_keylock::{AsyncEditor, Entry, EntryPresentationType, Menu, MessageSeverity, UserOption, UserSelection};
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;

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
        debug!("Waiting for password change...");
        super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver, &self.launcher)
    }

    fn show_menu(&self, menu: &Menu) -> Receiver<UserSelection> {
        debug!("Opening menu '{:?}'", menu);

        let instance_receiver_res = match menu {
            &Menu::Main => {
                self.jvm.invoke_to_channel(
                    &self.show_menu,
                    "apply",
                    &[InvocationArg::from("Main")])
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

    fn show_entries(&self, entries: Vec<Entry>, filter: String) -> Receiver<UserSelection> {
        let scala_entries: Vec<ScalaEntry> = entries.iter()
            .map(|entry| ScalaEntry::new(entry))
            .collect();
        let filter = if filter.is_empty() {
            "null".to_string()
        } else {
            filter
        };
        let instance_receiver_res = self.jvm.invoke_to_channel(
            &self.show_entries,
            "apply",
            &[
                InvocationArg::from((
                    scala_entries.as_slice(),
                    "org.rustkeylock.japi.ScalaEntry",
                    &self.jvm)),
                InvocationArg::from(filter)]);
        super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver_res, &self.launcher)
    }

    fn show_entry(&self, entry: Entry, index: usize, presentation_type: EntryPresentationType) -> Receiver<UserSelection> {
        let instance_receiver_res = match presentation_type {
            EntryPresentationType::View => {
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
            EntryPresentationType::Delete => {
                self.jvm.invoke_to_channel(
                    &self.show_entry,
                    "apply",
                    &[
                        InvocationArg::new(&ScalaEntry::new(&entry), "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(false),
                        InvocationArg::from(true)
                    ])
            }
            EntryPresentationType::Edit => {
                self.jvm.invoke_to_channel(
                    &self.show_entry,
                    "apply",
                    &[
                        InvocationArg::new(&ScalaEntry::new(&entry), "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(true),
                        InvocationArg::from(false)
                    ])
            }
        };

        super::callbacks::handle_instance_receiver_result(&self.jvm, instance_receiver_res, &self.launcher)
    }

    fn show_configuration(&self, nextcloud: NextcloudConfiguration, dropbox: DropboxConfiguration) -> Receiver<UserSelection> {
        let conf_strings = vec![
            nextcloud.server_url.clone(),
            nextcloud.username.clone(),
            nextcloud.decrypted_password().unwrap(),
            nextcloud.use_self_signed_certificate.to_string(),
            DropboxConfiguration::dropbox_url(),
            dropbox.decrypted_token().unwrap()];
        let instance_receiver_res = self.jvm.invoke_to_channel(
            &self.edit_configuration,
            "apply",
            &[InvocationArg::from((conf_strings.as_slice(), &self.jvm))]);
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