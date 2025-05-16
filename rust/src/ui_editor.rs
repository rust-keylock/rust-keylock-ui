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

use async_trait::async_trait;
use j4rs::{Instance, InvocationArg, Jvm};
use log::*;
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;
use rust_keylock::GeneralConfiguration;
use rust_keylock::{AsyncEditor, Entry, EntryMeta, EntryPresentationType, Menu, MessageSeverity, UserOption, UserSelection};
use serde::{Deserialize, Serialize};
use std::convert::TryFrom;
use std::sync::Mutex;
use zeroize::Zeroize;

use crate::errors;

pub struct DesktopImpl {
    show_menu: Mutex<Instance>,
    show_entries: Mutex<Instance>,
    show_entry: Mutex<Instance>,
    edit_configuration: Mutex<Instance>,
    show_message: Mutex<Instance>,
}

impl DesktopImpl {
    fn clone_show_menu(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_menu.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_show_entries(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_entries.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_show_entry(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_entry.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_edit_configuration(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.edit_configuration.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }

    fn clone_show_message(&self) -> errors::Result<Instance> {
        let jvm = Jvm::attach_thread()?;
        let i = self.show_message.try_lock()?;
        Ok(jvm.clone_instance(&i)?)
    }
}

pub fn new(jvm: &Jvm) -> errors::Result<DesktopImpl> {
    // Start the Ui
    debug!("Calling org.rustkeylock.ui.UiLauncher.launch");
    let _ = jvm
        .invoke_static("org.rustkeylock.ui.UiLauncher", "launch", InvocationArg::empty())?;

    debug!("Calling org.rustkeylock.ui.UiLauncher.getStage");
    let fx_stage = jvm
        .invoke_static("org.rustkeylock.ui.UiLauncher", "getStage", InvocationArg::empty())?;

    debug!("Stage retrieved. Proceeding...");

    // Create the Java classes that navigate the UI
    let show_menu = jvm
        .create_instance("org.rustkeylock.ui.callbacks.ShowMenuCb", &[InvocationArg::from(jvm.clone_instance(&fx_stage)?)])?;
    let show_entries = jvm
        .create_instance(
            "org.rustkeylock.ui.callbacks.ShowEntriesSetCb",
            &[InvocationArg::from(jvm.clone_instance(&fx_stage)?)],
        )?;
    let show_entry = jvm
        .create_instance("org.rustkeylock.ui.callbacks.ShowEntryCb", &[InvocationArg::from(jvm.clone_instance(&fx_stage)?)])?;
    let edit_configuration = jvm
        .create_instance(
            "org.rustkeylock.ui.callbacks.EditConfigurationCb",
            &[InvocationArg::from(jvm.clone_instance(&fx_stage)?)],
        )?;
    let show_message = jvm
        .create_instance("org.rustkeylock.ui.callbacks.ShowMessageCb", &[InvocationArg::from(jvm.clone_instance(&fx_stage).unwrap())])?;

    // Return the Editor
    Ok(DesktopImpl {
        show_menu: Mutex::new(show_menu),
        show_entries: Mutex::new(show_entries),
        show_entry: Mutex::new(show_entry),
        edit_configuration: Mutex::new(edit_configuration),
        show_message: Mutex::new(show_message),
    })
}

#[async_trait]
impl AsyncEditor for DesktopImpl {
    async fn show_password_enter(&self) -> UserSelection {
        show_password_enter(&self).await.unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_change_password(&self) -> UserSelection {
        show_change_password(&self).await.unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_menu(&self, menu: Menu) -> UserSelection {
        show_menu(&self, menu).await.unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_entries(&self, entries: Vec<Entry>, filter: String) -> UserSelection {
        show_entries(&self, entries, filter)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_entry(&self, entry: Entry, index: usize, presentation_type: EntryPresentationType) -> UserSelection {
        show_entry(&self, entry, index, presentation_type)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_configuration(
        &self,
        nextcloud: NextcloudConfiguration,
        dropbox: DropboxConfiguration,
        general: GeneralConfiguration,
    ) -> UserSelection {
        show_configuration(&self, nextcloud, dropbox, general)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }

    async fn exit(&self, contents_changed: bool) -> UserSelection {
        exit(&self, contents_changed).await.unwrap_or_else(|error| handle_error(&error))
    }

    async fn show_message(&self, message: &str, options: Vec<UserOption>, severity: MessageSeverity) -> UserSelection {
        show_message(&self, message, options, severity)
            .await
            .unwrap_or_else(|error| handle_error(&error))
    }
}

async fn show_password_enter(editor: &DesktopImpl) -> errors::Result<UserSelection> {
    debug!("Opening the password fragment");
    let instance_res_future =
        Jvm::invoke_into_sendable_async(editor.clone_show_menu()?, "apply".to_string(), vec![InvocationArg::try_from("TryPass")?]);
    super::callbacks::handle_instance_receiver_result(instance_res_future).await
}

async fn show_change_password(editor: &DesktopImpl) -> errors::Result<UserSelection> {
    debug!("Opening the change password fragment");
    let instance_res_future =
        Jvm::invoke_into_sendable_async(editor.clone_show_menu()?, "apply".to_string(), vec![InvocationArg::try_from("ChangePass")?]);
    super::callbacks::handle_instance_receiver_result(instance_res_future).await
}

async fn show_menu(editor: &DesktopImpl, menu: Menu) -> errors::Result<UserSelection> {
    debug!("Opening menu '{:?}'", menu);

    let instance_res_future = match menu {
        Menu::Main => {
            Jvm::invoke_into_sendable_async(editor.clone_show_menu()?, "apply".to_string(), vec![InvocationArg::try_from("Main")?])
        }
        Menu::NewEntry(ref entry_opt) => {
            let entry = entry_opt.clone().unwrap_or_else(|| Entry::empty());
            let empty_entry = JavaEntry::new(&entry);
            // In order to denote that this is a new entry, put -1 as index
            Jvm::invoke_into_sendable_async(
                editor.clone_show_entry()?,
                "apply".to_string(),
                vec![
                    InvocationArg::new(&empty_entry, "org.rustkeylock.japi.JavaEntry"),
                    InvocationArg::try_from(-1)?,
                    InvocationArg::try_from(true)?,
                    InvocationArg::try_from(false)?,
                ],
            )
        }
        Menu::ExportEntries => {
            Jvm::invoke_into_sendable_async(editor.clone_show_menu()?, "apply".to_string(), vec![InvocationArg::try_from("ExportEntries")?])
        }
        Menu::ImportEntries => {
            Jvm::invoke_into_sendable_async(editor.clone_show_menu()?, "apply".to_string(), vec![InvocationArg::try_from("ImportEntries")?])
        }
        Menu::Current => {
            Jvm::invoke_into_sendable_async(editor.clone_show_menu()?, "apply".to_string(), vec![InvocationArg::try_from("Current")?])
        }
        other => panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug to the developers.", other),
    };

    super::callbacks::handle_instance_receiver_result(instance_res_future).await
}

async fn show_entries(editor: &DesktopImpl, entries: Vec<Entry>, filter: String) -> errors::Result<UserSelection> {
    let java_entries: Vec<JavaEntry> = entries.iter().map(|entry| JavaEntry::new(entry)).collect();
    let filter = if filter.is_empty() { "null".to_string() } else { filter };
    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_show_entries()?,
        "apply".to_string(),
        vec![
            InvocationArg::try_from((java_entries.as_slice(), "org.rustkeylock.japi.JavaEntry"))?,
            InvocationArg::try_from(filter)?,
        ],
    );
    super::callbacks::handle_instance_receiver_result(instance_res_future).await
}

async fn show_entry(
    editor: &DesktopImpl,
    entry: Entry,
    index: usize,
    presentation_type: EntryPresentationType,
) -> errors::Result<UserSelection> {
    let instance_res_future = match presentation_type {
        EntryPresentationType::View => {
            Jvm::invoke_into_sendable_async(
                editor.clone_show_entry()?,
                "apply".to_string(),
                vec![
                    InvocationArg::new(&JavaEntry::new(&entry), "org.rustkeylock.japi.JavaEntry"),
                    InvocationArg::try_from(index as i32)?,
                    InvocationArg::try_from(false)?,
                    InvocationArg::try_from(false)?,
                ],
            )
        }
        EntryPresentationType::Delete => {
            Jvm::invoke_into_sendable_async(
                editor.clone_show_entry()?,
                "apply".to_string(),
                vec![
                    InvocationArg::new(&JavaEntry::new(&entry), "org.rustkeylock.japi.JavaEntry"),
                    InvocationArg::try_from(index as i32)?,
                    InvocationArg::try_from(false)?,
                    InvocationArg::try_from(true)?,
                ],
            )
        }
        EntryPresentationType::Edit => {
            Jvm::invoke_into_sendable_async(
                editor.clone_show_entry()?,
                "apply".to_string(),
                vec![
                    InvocationArg::new(&JavaEntry::new(&entry), "org.rustkeylock.japi.JavaEntry"),
                    InvocationArg::try_from(index as i32)?,
                    InvocationArg::try_from(true)?,
                    InvocationArg::try_from(false)?,
                ],
            )
        }
    };

    super::callbacks::handle_instance_receiver_result(instance_res_future).await
}

async fn show_configuration(
    editor: &DesktopImpl,
    nextcloud: NextcloudConfiguration,
    dropbox: DropboxConfiguration,
    general: GeneralConfiguration,
) -> errors::Result<UserSelection> {
    let conf_strings = vec![
        nextcloud.server_url.clone(),
        nextcloud.username.clone(),
        nextcloud.decrypted_password().unwrap().to_string(),
        nextcloud.use_self_signed_certificate.to_string(),
        DropboxConfiguration::dropbox_url(),
        dropbox.decrypted_token().unwrap().to_string(),
        general.browser_extension_token.unwrap_or_default(),
    ];

    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_edit_configuration()?,
        "apply".to_string(),
        vec![InvocationArg::try_from(conf_strings.as_slice())?],
    );
    super::callbacks::handle_instance_receiver_result(instance_res_future).await
}

async fn exit(editor: &DesktopImpl, contents_changed: bool) -> errors::Result<UserSelection> {
    debug!("Exiting rust-keylock ui...");
    if contents_changed {
        let instance_res_future = Jvm::invoke_into_sendable_async(
            editor.clone_show_menu()?,
            "apply".to_string(),
            vec![InvocationArg::try_from("Exit")?],
        );

        super::callbacks::handle_instance_receiver_result(instance_res_future).await
    } else {
        Ok(UserSelection::GoTo(Menu::ForceExit))
    }
}

async fn show_message(
    editor: &DesktopImpl,
    message: &str,
    options: Vec<UserOption>,
    severity: MessageSeverity,
) -> errors::Result<UserSelection> {
    debug!("Showing Message '{}'", message);
    let java_user_options: Vec<JavaUserOption> = options.iter().clone().map(|user_option| JavaUserOption::new(user_option)).collect();

    let instance_res_future = Jvm::invoke_into_sendable_async(
        editor.clone_show_message()?,
        "apply".to_string(),
        vec![
            InvocationArg::try_from((java_user_options.as_slice(), "org.rustkeylock.japi.JavaUserOption"))?,
            InvocationArg::try_from(message)?,
            InvocationArg::try_from(severity.to_string())?,
        ],
    );

    super::callbacks::handle_instance_receiver_result(instance_res_future).await
}

fn handle_error(error: &errors::RklUiError) -> UserSelection {
    error!("An error occured: {:?}", error);
    UserSelection::GoTo(Menu::Main)
}

#[derive(Serialize, Deserialize, Debug, Zeroize)]
#[zeroize(drop)]
pub(crate) struct JavaEntryMeta {
    pub leakedpassword: bool,
}

impl JavaEntryMeta {
    fn new(entry: &EntryMeta) -> JavaEntryMeta {
        JavaEntryMeta {
            leakedpassword: entry.leaked_password,
        }
    }
}

#[derive(Serialize, Deserialize, Debug, Zeroize)]
#[zeroize(drop)]
pub(crate) struct JavaEntry {
    pub name: String,
    pub url: String,
    pub user: String,
    pub pass: String,
    pub desc: String,
    pub meta: JavaEntryMeta,
}

impl JavaEntry {
    fn new(entry: &Entry) -> JavaEntry {
        JavaEntry {
            name: entry.name.clone(),
            url: entry.url.clone(),
            user: entry.user.clone(),
            pass: entry.pass.clone(),
            desc: entry.desc.clone(),
            meta: JavaEntryMeta::new(&entry.meta),
        }
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub(crate) struct JavaUserOption {
    pub label: String,
    pub value: String,
    pub short_label: String,
}

impl JavaUserOption {
    fn new(user_option: &UserOption) -> JavaUserOption {
        JavaUserOption {
            label: user_option.label.clone(),
            value: user_option.value.to_string(),
            short_label: user_option.short_label.clone(),
        }
    }
}

#[derive(Deserialize, Serialize, Debug)]
pub(crate) enum JavaMenu {
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

impl JavaMenu {
    pub(crate) fn to_menu(self) -> Menu {
        match self {
            JavaMenu::Main => Menu::Main,
            JavaMenu::Exit => Menu::Exit,
            JavaMenu::EntriesList { filter } => Menu::EntriesList(filter),
            JavaMenu::Save { b } => Menu::Save(b),
            JavaMenu::ChangePass => Menu::ChangePass,
            JavaMenu::ExportEntries => Menu::ExportEntries,
            JavaMenu::ImportEntries => Menu::ImportEntries,
            JavaMenu::ShowConfiguration => Menu::ShowConfiguration,
            JavaMenu::ForceExit => Menu::ForceExit,
            JavaMenu::NewEntry => Menu::NewEntry(None),
            JavaMenu::WaitForDbxTokenCallback { s } => Menu::WaitForDbxTokenCallback(s),
            JavaMenu::ShowEntry { idx } => Menu::ShowEntry(idx),
            JavaMenu::EditEntry { idx } => Menu::EditEntry(idx),
            JavaMenu::DeleteEntry { idx } => Menu::DeleteEntry(idx),
            _ => Menu::Current,
        }
    }
}
