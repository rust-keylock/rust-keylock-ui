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

use j4rs::{errors, Instance, InstanceReceiver, Jvm};
use log::*;
use rust_keylock::{Entry, Menu, UserOption, UserSelection, AllConfigurations};
use rust_keylock::nextcloud::NextcloudConfiguration;
use serde_derive::{Deserialize, Serialize};

use crate::ui_editor::{ScalaEntry, ScalaUserOption, ScalaMenu};
use rust_keylock::dropbox::DropboxConfiguration;

pub fn handle_instance_receiver_result(jvm: &Jvm, instance_receiver_res: errors::Result<InstanceReceiver>, launcher: &Instance) -> Receiver<UserSelection> {
    let (tx, rx) = mpsc::channel();
    let handler_instance_receiver = jvm.invoke_to_channel(
        &launcher,
        "initHandler",
        &[]).expect("Could not register the Launcher callback");

    let _ = thread::spawn(move || {
        let jvm = Jvm::attach_thread().unwrap();
        let sel = retrieve_user_selection(&jvm, &handler_instance_receiver, instance_receiver_res);
        let _ = tx.send(sel);
    });

    rx
}

fn retrieve_user_selection(jvm: &Jvm, handler_instance_receiver: &InstanceReceiver, instance_receiver_res: errors::Result<InstanceReceiver>) -> UserSelection {
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
                UserSelection::ProvidedPassword(password, number)
            }
            GuiResponse::GoToMenu { menu } => {
                UserSelection::GoTo(menu.to_menu())
            }
            GuiResponse::AddEntry { entry } => {
                debug!("add_entry");
                let entry = Entry::new(entry.name,
                                       entry.url,
                                       entry.user,
                                       entry.pass,
                                       entry.desc);

                UserSelection::NewEntry(entry)
            }
            GuiResponse::ReplaceEntry { entry, index } => {
                debug!("replace_entry");
                let entry = Entry::new(entry.name,
                                       entry.url,
                                       entry.user,
                                       entry.pass,
                                       entry.desc);

                UserSelection::ReplaceEntry(index as usize, entry)
            }
            GuiResponse::DeleteEntry { index } => {
                debug!("delete_entry");
                UserSelection::DeleteEntry(index)
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
                    UserSelection::ImportFrom(path, password, number as usize)
                }
            }
            GuiResponse::Copy { data } => {
                debug!("copy");
                UserSelection::AddToClipboard(data)
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
    GoToMenu { menu: ScalaMenu },
    AddEntry { entry: ScalaEntry },
    ReplaceEntry { entry: ScalaEntry, index: usize },
    DeleteEntry { index: usize },
    SetConfiguration { strings: Vec<String> },
    UserOptionSelected { user_option: ScalaUserOption },
    ExportImport { path: String, mode: usize, password: String, number: usize },
    Copy { data: String },
}
