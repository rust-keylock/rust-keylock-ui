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

use std::future::Future;

use futures::future::{self, Either};
use futures::pin_mut;
use j4rs::errors::J4RsError;
use j4rs::{Instance, InvocationArg, Jvm};
use log::*;
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;
use rust_keylock::{AllConfigurations, Entry, EntryMeta, GeneralConfiguration, UserOption, UserSelection};
use serde::{Deserialize, Serialize};

use crate::ui_editor::{JavaEntry, JavaMenu, JavaUserOption};

pub async fn handle_instance_receiver_result(
    instance_res_future: impl Future<Output = Result<Instance, J4RsError>>,
) -> crate::errors::Result<UserSelection> {
    let ui_stopper = {
        let jvm = Jvm::attach_thread()?;
        jvm.invoke_static("org.rustkeylock.ui.UiLauncher", "getNewUiStopper", InvocationArg::empty())?
    };
    let window_exit_future = Jvm::invoke_into_sendable_async(ui_stopper, "getExitFuture".to_string(), vec![]);

    pin_mut!(instance_res_future);
    pin_mut!(window_exit_future);

    let received_instance_res = match future::select(instance_res_future, window_exit_future).await {
        Either::Left((user_response_instance, _)) => user_response_instance,
        Either::Right((window_exit_instance, _)) => window_exit_instance,
    };
    handle_instance(received_instance_res?)
}

fn handle_instance(instance: Instance) -> crate::errors::Result<UserSelection> {
    let jvm = Jvm::attach_thread()?;
    let gui_response = jvm.to_rust(instance)?;

    let user_selection = match gui_response {
        GuiResponse::ProvidedPassword { password, number } => UserSelection::new_provided_password(password, number),
        GuiResponse::GoToMenu { menu } => UserSelection::GoTo(menu.to_menu()),
        GuiResponse::AddEntry { entry } => {
            debug!("add_entry");
            let meta = EntryMeta::new(entry.meta.leakedpassword);
            let entry = Entry::new(
                entry.name.to_owned(),
                entry.url.to_owned(),
                entry.user.to_owned(),
                entry.pass.to_owned(),
                entry.desc.to_owned(),
                meta,
            );

            UserSelection::NewEntry(entry)
        }
        GuiResponse::ReplaceEntry { entry, index } => {
            debug!("replace_entry");
            let meta = EntryMeta::new(entry.meta.leakedpassword);
            let entry = Entry::new(
                entry.name.to_owned(),
                entry.url.to_owned(),
                entry.user.to_owned(),
                entry.pass.to_owned(),
                entry.desc.to_owned(),
                meta,
            );

            UserSelection::ReplaceEntry(index as usize, entry)
        }
        GuiResponse::DeleteEntry { index } => {
            debug!("delete_entry");
            UserSelection::DeleteEntry(index)
        }
        GuiResponse::GeneratePassphrase { entry, index } => {
            debug!("generate_passphrase");
            let meta = EntryMeta::new(entry.meta.leakedpassword);
            let entry = Entry::new(
                entry.name.to_owned(),
                entry.url.to_owned(),
                entry.user.to_owned(),
                entry.pass.to_owned(),
                entry.desc.to_owned(),
                meta,
            );
            let opt = if index < 0 { None } else { Some(index as usize) };
            UserSelection::GeneratePassphrase(opt, entry)
        }
        GuiResponse::SetConfiguration { strings } => {
            debug!("set_configuration with {} elements", strings.len());

            let ncc = if strings.len() == 6 {
                let b = match strings[3].as_ref() {
                    "true" => true,
                    _ => false,
                };
                NextcloudConfiguration::new(strings[0].clone(), strings[1].clone(), strings[2].clone(), b)
            } else {
                NextcloudConfiguration::new(
                    "Wrong Java Data".to_string().to_string(),
                    "Wrong Java Data".to_string(),
                    "Wrong Java Data".to_string(),
                    false,
                )
            };

            let dbxc = if strings.len() == 6 {
                DropboxConfiguration::new(strings[4].clone())
            } else {
                Ok(DropboxConfiguration::default())
            };

            let genc = if strings.len() == 6 {
                GeneralConfiguration::new(Some(strings[5].clone()))
            } else {
                GeneralConfiguration::default()
            };
            UserSelection::UpdateConfiguration(AllConfigurations::new(ncc.unwrap(), dbxc.unwrap(), genc))
        }
        GuiResponse::UserOptionSelected { user_option } => {
            debug!("user_option_selected");

            UserSelection::UserOption(UserOption::from((user_option.label, user_option.value, user_option.short_label)))
        }
        GuiResponse::ExportImport {
            path,
            mode,
            password,
            number,
        } => {
            debug!("export_import");

            if mode > 0 {
                debug!("Followed exporting path");
                UserSelection::ExportTo(path)
            } else {
                debug!("Followed importing path");
                UserSelection::new_import_from(path, password, number as usize)
            }
        }
        GuiResponse::Copy { data } => {
            debug!("copy");
            UserSelection::AddToClipboard(data)
        }
        GuiResponse::CheckPasswords => {
            debug!("CheckPasswords");
            UserSelection::CheckPasswords
        }
        GuiResponse::GenerateBrowserExtensionToken => {
            debug!("GenerateBrowserExtensionToken");
            UserSelection::GenerateBrowserExtensionToken
        }
    };

    Ok(user_selection)
}

#[derive(Deserialize, Serialize, Debug)]
pub(crate) enum GuiResponse {
    ProvidedPassword {
        password: String,
        number: usize,
    },
    GoToMenu {
        menu: JavaMenu,
    },
    AddEntry {
        entry: JavaEntry,
    },
    ReplaceEntry {
        entry: JavaEntry,
        index: usize,
    },
    DeleteEntry {
        index: usize,
    },
    SetConfiguration {
        strings: Vec<String>,
    },
    UserOptionSelected {
        user_option: JavaUserOption,
    },
    ExportImport {
        path: String,
        mode: usize,
        password: String,
        number: usize,
    },
    Copy {
        data: String,
    },
    GeneratePassphrase {
        entry: JavaEntry,
        index: isize,
    },
    CheckPasswords,
    GenerateBrowserExtensionToken,
}
