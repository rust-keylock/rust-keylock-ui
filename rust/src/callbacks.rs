use ::ui_editor::{ScalaEntry, ScalaUserOption};
use j4rs::{Instance, Jvm};
use rust_keylock::{Entry, Menu, UserOption, UserSelection};
use rust_keylock::nextcloud::NextcloudConfiguration;
use super::TX;

pub fn ui_callback(jvm: Jvm, inst: Instance) {
    match TX.try_lock() {
        Ok(tx_opt) => {
            debug!("Lock acquired");
            let tx = tx_opt.as_ref().unwrap();
            let res = jvm.to_rust(inst);
            if let Ok(gr) = res {
                match gr {
                    GuiResponse::ProvidedPassword { password, number } => {
                        let user_selection = UserSelection::ProvidedPassword(password, number);
                        tx.send(user_selection).unwrap();
                        debug!("set_password sent to the TX");
                    }
                    GuiResponse::GoToMenu { menu } => {
                        debug!("go_to_menu called with {}", menu);
                        let menu = Menu::from(menu, None, None);
                        let user_selection = UserSelection::GoTo(menu);
                        tx.send(user_selection).unwrap();
                        debug!("go_to_menu sent UserSelection to the TX");
                    }
                    GuiResponse::GoToMenuPlusArgs { menu, intarg, stringarg } => {
                        debug!("go_to_menu_plus_arg: menu: {}, num = '{}' and str = '{}'",
                               menu,
                               intarg,
                               stringarg);

                        let num_opt = if intarg == "null" {
                            None
                        } else {
                            let num = intarg.parse::<usize>().unwrap();
                            Some(num)
                        };

                        let str_opt = if stringarg == "null" {
                            None
                        } else {
                            Some(stringarg)
                        };

                        let menu = Menu::from(menu, num_opt, str_opt);
                        let user_selection = UserSelection::GoTo(menu);
                        tx.send(user_selection).unwrap();
                        debug!("go_to_menu_plus_arg sent UserSelection to the TX");
                    }
                    GuiResponse::AddEntry { entry } => {
                        debug!("add_entry");
                        let entry = Entry::new(entry.name,
                                               entry.url,
                                               entry.user,
                                               entry.pass,
                                               entry.desc);

                        let user_selection = UserSelection::NewEntry(entry);
                        tx.send(user_selection).unwrap();
                        debug!("add_entry sent UserSelection to the TX");
                    }
                    GuiResponse::ReplaceEntry { entry, index } => {
                        debug!("replace_entry");
                        let entry = Entry::new(entry.name,
                                               entry.url,
                                               entry.user,
                                               entry.pass,
                                               entry.desc);

                        let user_selection = UserSelection::ReplaceEntry(index as usize, entry);
                        tx.send(user_selection).unwrap();
                        debug!("replace_entry sent UserSelection to the TX");
                    }
                    GuiResponse::DeleteEntry { index } => {
                        debug!("delete_entry");
                        let user_selection = UserSelection::DeleteEntry(index);
                        tx.send(user_selection).unwrap();
                        debug!("delete_entry sent UserSelection to the TX");
                    }
                    GuiResponse::SetConfiguration { strings } => {
                        debug!("set_configuration with {} elements", strings.len());

                        let ncc = if strings.len() == 4 {
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

                        let conf = UserSelection::UpdateConfiguration(ncc.unwrap());
                        tx.send(conf).unwrap();
                        debug!("set_configuration sent UserSelection to the TX");
                    }
                    GuiResponse::UserOptionSelected { user_option } => {
                        debug!("user_option_selected");

                        tx.send(UserSelection::UserOption(
                            UserOption::from((
                                user_option.label,
                                user_option.value,
                                user_option.short_label)
                            )
                        )).unwrap();
                        debug!("user_option_selected sent UserSelection to the TX");
                    }
                    GuiResponse::ExportImport { path, mode, password, number } => {
                        debug!("export_import");

                        let user_selection = if mode > 0 {
                            debug!("Followed exporting path");
                            UserSelection::ExportTo(path)
                        } else {
                            debug!("Followed importing path");
                            UserSelection::ImportFrom(path, password, number as usize)
                        };
                        tx.send(user_selection).unwrap();
                        debug!("export_import sent UserSelection to the TX");
                    }
                }
            } else {
                error!("Error while creating Rust representation of a Java Instance: {:?}", res.err());
            }
        }
        Err(error) => {
            error!("Could not acquire lock for tx: {:?}", error);
        }
    };
}

#[derive(Deserialize, Debug)]
enum GuiResponse {
    ProvidedPassword { password: String, number: usize },
    GoToMenu { menu: String },
    GoToMenuPlusArgs { menu: String, intarg: String, stringarg: String },
    AddEntry { entry: ScalaEntry },
    ReplaceEntry { entry: ScalaEntry, index: usize },
    DeleteEntry { index: usize },
    SetConfiguration { strings: Vec<String> },
    UserOptionSelected { user_option: ScalaUserOption },
    ExportImport { path: String, mode: usize, password: String, number: usize },
}
