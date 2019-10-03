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
use std::convert::TryFrom;
use std::error::Error;

use j4rs::{Instance, InvocationArg, Jvm};
use log::*;
use serde::{Deserialize, Serialize};

use rust_keylock::{AsyncEditor, Entry, EntryPresentationType, Menu, MessageSeverity, UserOption, UserSelection};
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;
use crate::errors;

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
        show_password_enter(&self).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_change_password(&self) -> Receiver<UserSelection> {
        show_change_password(&self).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_menu(&self, menu: &Menu) -> Receiver<UserSelection> {
        show_menu(&self, menu).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_entries(&self, entries: Vec<Entry>, filter: String) -> Receiver<UserSelection> {
        show_entries(&self, entries, filter).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_entry(&self, entry: Entry, index: usize, presentation_type: EntryPresentationType) -> Receiver<UserSelection> {
        show_entry(&self, entry, index, presentation_type).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_configuration(&self, nextcloud: NextcloudConfiguration, dropbox: DropboxConfiguration) -> Receiver<UserSelection> {
        show_configuration(&self, nextcloud, dropbox).unwrap_or_else(|error| handle_error(&error))
    }

    fn exit(&self, contents_changed: bool) -> Receiver<UserSelection> {
        exit(&self, contents_changed).unwrap_or_else(|error| handle_error(&error))
    }

    fn show_message(&self,
                    message: &str,
                    options: Vec<UserOption>,
                    severity: MessageSeverity)
                    -> Receiver<UserSelection> {
        show_message(&self, message, options, severity).unwrap_or_else(|error| handle_error(&error))
    }
}

fn show_password_enter(editor: &DesktopImpl) -> errors::Result<Receiver<UserSelection>> {
    debug!("Opening the password fragment");
    let instance_receiver = editor.jvm.invoke_to_channel(
        &editor.show_menu,
        "apply",
        &[InvocationArg::try_from("TryPass").unwrap()]);
    debug!("Waiting for password...");
    super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver, &editor.launcher)
}

fn show_change_password(editor: &DesktopImpl) -> errors::Result<Receiver<UserSelection>> {
    debug!("Opening the change password fragment");
    let instance_receiver = editor.jvm.invoke_to_channel(
        &editor.show_menu,
        "apply",
        &[InvocationArg::try_from("ChangePass").unwrap()]);
    debug!("Waiting for password change...");
    super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver, &editor.launcher)
}

fn show_menu(editor: &DesktopImpl, menu: &Menu) -> errors::Result<Receiver<UserSelection>> {
    debug!("Opening menu '{:?}'", menu);

    let instance_receiver_res = match menu {
        &Menu::Main => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu,
                "apply",
                &[InvocationArg::try_from("Main").unwrap()])
        }
        &Menu::NewEntry => {
            let empty_entry = ScalaEntry::empty();
            // In order to denote that this is a new entry, put -1 as index
            editor.jvm.invoke_to_channel(
                &editor.show_entry,
                "apply",
                &[
                    InvocationArg::new(&empty_entry, "org.rustkeylock.japi.ScalaEntry"),
                    InvocationArg::try_from(-1).unwrap(),
                    InvocationArg::try_from(true).unwrap(),
                    InvocationArg::try_from(false).unwrap()
                ])
        }
        &Menu::ExportEntries => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu,
                "apply",
                &[InvocationArg::try_from("ExportEntries").unwrap()])
        }
        &Menu::ImportEntries => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu,
                "apply",
                &[InvocationArg::try_from("ImportEntries").unwrap()])
        }
        &Menu::Current => {
            editor.jvm.invoke_to_channel(
                &editor.show_menu,
                "apply",
                &[InvocationArg::try_from("Current").unwrap()])
        }
        other => panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug to the developers.", other),
    };

    super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver_res, &editor.launcher)
}

fn show_entries(editor: &DesktopImpl, entries: Vec<Entry>, filter: String) -> errors::Result<Receiver<UserSelection>> {
    let scala_entries: Vec<ScalaEntry> = entries.iter()
        .map(|entry| ScalaEntry::new(entry))
        .collect();
    let filter = if filter.is_empty() {
        "null".to_string()
    } else {
        filter
    };
    let instance_receiver_res = editor.jvm.invoke_to_channel(
        &editor.show_entries,
        "apply",
        &[
            InvocationArg::try_from((
                scala_entries.as_slice(),
                "org.rustkeylock.japi.ScalaEntry"))
                .unwrap(),
            InvocationArg::try_from(filter).unwrap()]);
    super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver_res, &editor.launcher)
}

fn show_entry(editor: &DesktopImpl, entry: Entry, index: usize, presentation_type: EntryPresentationType) -> errors::Result<Receiver<UserSelection>> {
    let instance_receiver_res = match presentation_type {
        EntryPresentationType::View => {
            editor.jvm.invoke_to_channel(
                &editor.show_entry,
                "apply",
                &[
                    InvocationArg::new(&ScalaEntry::new(&entry), "org.rustkeylock.japi.ScalaEntry"),
                    InvocationArg::try_from(index as i32).unwrap(),
                    InvocationArg::try_from(false).unwrap(),
                    InvocationArg::try_from(false).unwrap()
                ])
        }
        EntryPresentationType::Delete => {
            editor.jvm.invoke_to_channel(
                &editor.show_entry,
                "apply",
                &[
                    InvocationArg::new(&ScalaEntry::new(&entry), "org.rustkeylock.japi.ScalaEntry"),
                    InvocationArg::try_from(index as i32).unwrap(),
                    InvocationArg::try_from(false).unwrap(),
                    InvocationArg::try_from(true).unwrap(),
                ])
        }
        EntryPresentationType::Edit => {
            editor.jvm.invoke_to_channel(
                &editor.show_entry,
                "apply",
                &[
                    InvocationArg::new(&ScalaEntry::new(&entry), "org.rustkeylock.japi.ScalaEntry"),
                    InvocationArg::try_from(index as i32).unwrap(),
                    InvocationArg::try_from(true).unwrap(),
                    InvocationArg::try_from(false).unwrap()
                ])
        }
    };

    super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver_res, &editor.launcher)
}

fn show_configuration(editor: &DesktopImpl, nextcloud: NextcloudConfiguration, dropbox: DropboxConfiguration) -> errors::Result<Receiver<UserSelection>> {
    let conf_strings = vec![
        nextcloud.server_url.clone(),
        nextcloud.username.clone(),
        nextcloud.decrypted_password().unwrap(),
        nextcloud.use_self_signed_certificate.to_string(),
        DropboxConfiguration::dropbox_url(),
        dropbox.decrypted_token().unwrap()];
    let instance_receiver_res = editor.jvm.invoke_to_channel(
        &editor.edit_configuration,
        "apply",
        &[InvocationArg::try_from(conf_strings.as_slice()).unwrap()]);
    super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver_res, &editor.launcher)
}

fn exit(editor: &DesktopImpl, contents_changed: bool) -> errors::Result<Receiver<UserSelection>> {
    debug!("Exiting rust-keylock ui...");
    if contents_changed {
        let instance_receiver = editor.jvm.invoke_to_channel(
            &editor.show_menu,
            "apply",
            &[InvocationArg::try_from("Exit").unwrap()]);

        super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver, &editor.launcher)
    } else {
        let (tx, rx) = mpsc::channel();
        let _ = tx.send(UserSelection::GoTo(Menu::ForceExit));
        Ok(rx)
    }
}

fn show_message(editor: &DesktopImpl, message: &str, options: Vec<UserOption>, severity: MessageSeverity) -> errors::Result<Receiver<UserSelection>> {
    debug!("Showing Message '{}'", message);
    let scala_user_options: Vec<ScalaUserOption> = options.iter()
        .clone()
        .map(|user_option| ScalaUserOption::new(user_option))
        .collect();
    let instance_receiver = editor.jvm.invoke_to_channel(
        &editor.show_message,
        "apply",
        &[
            InvocationArg::try_from((
                scala_user_options.as_slice(),
                "org.rustkeylock.japi.ScalaUserOption")).unwrap(),
            InvocationArg::try_from(message).unwrap(),
            InvocationArg::try_from(severity.to_string()).unwrap()]);

    super::callbacks::handle_instance_receiver_result(&editor.jvm, instance_receiver, &editor.launcher)
}

fn handle_error(error: &errors::RklUiError) -> Receiver<UserSelection> {
    error!("An error occured: {}", error.description());
    error!("{:?}", error);
    let (tx, rx) = mpsc::channel();
    let _ = tx.send(UserSelection::GoTo(Menu::Main));
    rx
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