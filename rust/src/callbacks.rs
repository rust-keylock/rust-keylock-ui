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

use std::{thread, time};
use std::sync::mpsc::{self, Receiver, TryRecvError};

use j4rs::{self, InvocationArg};
use j4rs::{Instance, InstanceReceiver, Jvm};
use log::*;
use rust_keylock::{AllConfigurations, Entry, EntryMeta, Menu, UserOption, UserSelection};
use rust_keylock::dropbox::DropboxConfiguration;
use rust_keylock::nextcloud::NextcloudConfiguration;
use serde::{Deserialize, Serialize};

use crate::ui_editor::{JavaEntry, JavaMenu, JavaUserOption};

pub fn handle_instance_receiver_result(jvm: &Jvm, instance_receiver_res: j4rs::errors::Result<InstanceReceiver>) -> crate::errors::Result<Receiver<UserSelection>> {
    let (tx, rx) = mpsc::channel();
    let ui_stopper = jvm.invoke_static("org.rustkeylock.ui.UiLauncher", "initOnCloseHandler", InvocationArg::empty()).expect("Could not retrieve the UI Stopper");
    let handler_instance_receiver = jvm.init_callback_channel(&ui_stopper).expect("Could not register the Launcher callback");

    let _ = thread::spawn(move || {
        let jvm = Jvm::attach_thread().unwrap();
        let sel = retrieve_user_selection(&jvm, &handler_instance_receiver, instance_receiver_res);
        let _ = tx.send(sel);
    });

    Ok(rx)
}

fn retrieve_user_selection(jvm: &Jvm, handler_instance_receiver: &InstanceReceiver, instance_receiver_res: j4rs::errors::Result<InstanceReceiver>) -> UserSelection {
    match instance_receiver_res {
        Ok(instance_receiver) => {
            let user_selection;

            // The select macro is a nightly feature and is going to be deprecated. Use polling until a better solution is found.
            // https://github.com/rust-lang/rust/issues/27800
            loop {
                let park_millis = time::Duration::from_millis(10);

                match (instance_receiver.rx().try_recv(), handler_instance_receiver.rx().try_recv()) {
                    // Match the handler instance receiver first, as it sends messages for exiting
                    (_, Ok(instance)) => {
                        user_selection = handle_instance(jvm, instance);
                        break;
                    }
                    (Ok(instance), _) => {
                        user_selection = handle_instance(jvm, instance);
                        break;
                    }
                    (Err(TryRecvError::Disconnected), _) => {
                        warn!("The UI channel got disconnected. Returning to the main Menu");
                        user_selection = UserSelection::GoTo(Menu::Main);
                        break;
                    }
                    (_, Err(TryRecvError::Disconnected)) => {
                        warn!("The handler instance channel got disconnected. Returning to the main Menu");
                        user_selection = UserSelection::GoTo(Menu::Main);
                        break;
                    }
                    (Err(TryRecvError::Empty), Err(TryRecvError::Empty)) => { /* keep looping */ }
                }

                thread::park_timeout(park_millis);
            }

            user_selection
        }
        Err(error) => {
            error!("Error while invoking invoke_to_channel: {:?}", error);
            UserSelection::GoTo(Menu::Main)
        }
    }
}

fn handle_instance(jvm: &Jvm, instance: Instance) -> UserSelection {
    let res = jvm.to_rust(instance);
    if let Ok(gr) = res {
        match gr {
            GuiResponse::ProvidedPassword { password, number } => {
                UserSelection::new_provided_password(password, number)
            }
            GuiResponse::GoToMenu { menu } => {
                UserSelection::GoTo(menu.to_menu())
            }
            GuiResponse::AddEntry { entry } => {
                debug!("add_entry");
                let meta = EntryMeta::new(entry.meta.leakedpassword);
                let entry = Entry::new(entry.name.to_owned(),
                                       entry.url.to_owned(),
                                       entry.user.to_owned(),
                                       entry.pass.to_owned(),
                                       entry.desc.to_owned(),
                                       meta);

                UserSelection::NewEntry(entry)
            }
            GuiResponse::ReplaceEntry { entry, index } => {
                debug!("replace_entry");
                let meta = EntryMeta::new(entry.meta.leakedpassword);
                let entry = Entry::new(entry.name.to_owned(),
                                       entry.url.to_owned(),
                                       entry.user.to_owned(),
                                       entry.pass.to_owned(),
                                       entry.desc.to_owned(),
                                       meta);

                UserSelection::ReplaceEntry(index as usize, entry)
            }
            GuiResponse::DeleteEntry { index } => {
                debug!("delete_entry");
                UserSelection::DeleteEntry(index)
            }
            GuiResponse::GeneratePassphrase { entry, index } => {
                debug!("generate_passphrase");
                let meta = EntryMeta::new(entry.meta.leakedpassword);
                let entry = Entry::new(entry.name.to_owned(),
                                       entry.url.to_owned(),
                                       entry.user.to_owned(),
                                       entry.pass.to_owned(),
                                       entry.desc.to_owned(),
                                       meta);
                let opt = if index < 0 {
                    None
                } else {
                    Some(index as usize)
                };
                UserSelection::GeneratePassphrase(opt, entry)
            }
            GuiResponse::SetConfiguration { strings } => {
                debug!("set_configuration with {} elements", strings.len());

                let ncc = if strings.len() == 5 {
                    let b = match strings[3].as_ref() {
                        "true" => true,
                        _ => false,
                    };
                    NextcloudConfiguration::new(strings[0].clone(),
                                                strings[1].clone(),
                                                strings[2].clone(),
                                                b)
                } else {
                    NextcloudConfiguration::new("Wrong Java Data".to_string().to_string(),
                                                "Wrong Java Data".to_string(),
                                                "Wrong Java Data".to_string(),
                                                false)
                };

                let dbxc = if strings.len() == 5 {
                    DropboxConfiguration::new(strings[4].clone())
                } else {
                    Ok(DropboxConfiguration::default())
                };

                UserSelection::UpdateConfiguration(AllConfigurations::new(ncc.unwrap(), dbxc.unwrap()))
            }
            GuiResponse::UserOptionSelected { user_option } => {
                debug!("user_option_selected");

                UserSelection::UserOption(
                    UserOption::from((
                        user_option.label,
                        user_option.value,
                        user_option.short_label)
                    )
                )
            }
            GuiResponse::ExportImport { path, mode, password, number } => {
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
        }
    } else {
        error!("Error while creating Rust representation of a Java Instance: {:?}", res.err());
        UserSelection::GoTo(Menu::Main)
    }
}

#[derive(Deserialize, Serialize, Debug)]
pub(crate) enum GuiResponse {
    ProvidedPassword { password: String, number: usize },
    GoToMenu { menu: JavaMenu },
    AddEntry { entry: JavaEntry },
    ReplaceEntry { entry: JavaEntry, index: usize },
    DeleteEntry { index: usize },
    SetConfiguration { strings: Vec<String> },
    UserOptionSelected { user_option: JavaUserOption },
    ExportImport { path: String, mode: usize, password: String, number: usize },
    Copy { data: String },
    GeneratePassphrase { entry: JavaEntry, index: isize },
    CheckPasswords,
}
