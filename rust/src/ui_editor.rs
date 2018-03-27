use j4rs::{Instance, InvocationArg, Jvm};
use rust_keylock::{Editor, Entry, Menu, MessageSeverity, RklConfiguration, Safe, UserOption, UserSelection};
use std::sync::mpsc::Receiver;
use super::logger;

pub struct AndroidImpl {
    jvm: Jvm,
    show_menu: Instance,
    show_entries: Instance,
    show_entry: Instance,
    edit_configuration: Instance,
    show_message: Instance,
    rx: Receiver<UserSelection>,
}

pub fn new(jvm: Jvm, rx: Receiver<UserSelection>) -> AndroidImpl {
    // Start the Ui
    let launcher = jvm.invoke_static(
        "org.rustkeylock.japi.Launcher",
        "start",
        &Vec::new())
        .unwrap();
    // Do the initialization tasks and set the On close event handler
    let _ = jvm.invoke_async(
        &launcher,
        "initHandler",
        &vec![],
        super::callbacks::ui_callback);
    let fx_stage = jvm.invoke_static("org.rustkeylock.japi.Launcher", "getStage", &Vec::new()).unwrap();
    // Create the Java classes that navigate the UI
    let show_menu = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowMenuCb",
        &vec![InvocationArg::from(fx_stage.clone())]).unwrap();
    let show_entries = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowEntriesSetCb",
        &vec![InvocationArg::from(fx_stage.clone())]).unwrap();
    let show_entry = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowEntryCb",
        &vec![InvocationArg::from(fx_stage.clone())]).unwrap();
    let edit_configuration = jvm.create_instance(
        "org.rustkeylock.callbacks.EditConfigurationCb",
        &vec![InvocationArg::from(fx_stage.clone())]).unwrap();
    let show_message = jvm.create_instance(
        "org.rustkeylock.callbacks.ShowMessageCb",
        &vec![InvocationArg::from(fx_stage.clone())]).unwrap();
    // Return the Editor
    AndroidImpl {
        jvm: jvm,
        show_menu: show_menu,
        show_entries: show_entries,
        show_entry: show_entry,
        edit_configuration: edit_configuration,
        show_message: show_message,
        rx: rx,
    }
}

impl Editor for AndroidImpl {
    fn show_password_enter(&self) -> UserSelection {
        println!("Opening the password fragment");
        let try_pass_menu_name = Menu::TryPass.get_name();
        let _ = self.jvm.invoke_async(
            &self.show_menu,
            "apply",
            &vec![InvocationArg::from(try_pass_menu_name)],
            super::callbacks::ui_callback);
        println!("Waiting for password...");
        let user_selection = self.rx.recv().unwrap();
        user_selection
    }

    fn show_change_password(&self) -> UserSelection {
        debug!("Opening the change password fragment");
        let change_pass_menu_name = Menu::ChangePass.get_name();
        let _ = self.jvm.invoke_async(
            &self.show_menu,
            "apply",
            &vec![InvocationArg::from(change_pass_menu_name)],
            super::callbacks::ui_callback);
        debug!("Waiting for password...");
        let user_selection = self.rx.recv().unwrap();
        user_selection
    }

    fn show_menu(&self, menu: &Menu, safe: &Safe, configuration: &RklConfiguration) -> UserSelection {
        debug!("Opening menu '{:?}' with entries size {}", menu, safe.get_entries().len());

        match menu {
            &Menu::Main => {
                let _ = self.jvm.invoke_async(
                    &self.show_menu,
                    "apply",
                    &vec![InvocationArg::from(Menu::Main.get_name())],
                    super::callbacks::ui_callback);
            }
            &Menu::EntriesList(_) => {
                let scala_entries: Vec<ScalaEntry> = safe.get_entries().iter()
                    .map(|entry| ScalaEntry::new(entry))
                    .collect();
                let filter = if safe.get_filter().len() == 0 {
                    "null".to_string()
                } else {
                    safe.get_filter().clone()
                };
                let _ = self.jvm.invoke_async(
                    &self.show_entries,
                    "apply",
                    &vec![
                        InvocationArg::from((
                            scala_entries.as_slice(),
                            "org.rustkeylock.japi.ScalaEntry",
                            &self.jvm)),
                        InvocationArg::from(filter)],
                    super::callbacks::ui_callback);
            }
            &Menu::ShowEntry(index) => {
                let entry = safe.get_entry_decrypted(index);
                let _ = self.jvm.invoke_async(
                    &self.show_entry,
                    "apply",
                    &vec![
                        InvocationArg::new(&ScalaEntry::new(&entry), "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(false),
                        InvocationArg::from(false)
                    ],
                    super::callbacks::ui_callback);
            }
            &Menu::DeleteEntry(index) => {
                let entry = ScalaEntry::new(safe.get_entry(index));

                let _ = self.jvm.invoke_async(
                    &self.show_entry,
                    "apply",
                    &vec![
                        InvocationArg::new(&entry, "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(false),
                        InvocationArg::from(true)
                    ],
                    super::callbacks::ui_callback);
            }
            &Menu::NewEntry => {
                let empty_entry = ScalaEntry::empty();
                // In order to denote that this is a new entry, put -1 as index
                let _ = self.jvm.invoke_async(
                    &self.show_entry,
                    "apply",
                    &vec![
                        InvocationArg::new(&empty_entry, "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(-1),
                        InvocationArg::from(true),
                        InvocationArg::from(false)
                    ],
                    super::callbacks::ui_callback);
            }
            &Menu::EditEntry(index) => {
                let ref selected_entry = safe.get_entry_decrypted(index);
                let _ = self.jvm.invoke_async(
                    &self.show_entry,
                    "apply",
                    &vec![
                        InvocationArg::new(&ScalaEntry::new(&selected_entry), "org.rustkeylock.japi.ScalaEntry"),
                        InvocationArg::from(index as i32),
                        InvocationArg::from(true),
                        InvocationArg::from(false)
                    ],
                    super::callbacks::ui_callback);
            }
            &Menu::ExportEntries => {
                let _ = self.jvm.invoke_async(
                    &self.show_menu,
                    "apply",
                    &vec![InvocationArg::from(Menu::ExportEntries.get_name())],
                    super::callbacks::ui_callback);
            }
            &Menu::ImportEntries => {
                let _ = self.jvm.invoke_async(
                    &self.show_menu,
                    "apply",
                    &vec![InvocationArg::from(Menu::ImportEntries.get_name())],
                    super::callbacks::ui_callback);
            }
            &Menu::ShowConfiguration => {
                let conf_strings = vec![
                    configuration.nextcloud.server_url.clone(),
                    configuration.nextcloud.username.clone(),
                    configuration.nextcloud.decrypted_password().unwrap(),
                    configuration.nextcloud.use_self_signed_certificate.to_string()];
                let _ = self.jvm.invoke_async(
                    &self.edit_configuration,
                    "apply",
                    &vec![InvocationArg::from((conf_strings.as_slice(), &self.jvm))],
                    super::callbacks::ui_callback);
            }
            other => panic!("Menu '{:?}' cannot be used with Entries. Please, consider opening a bug to the developers.", other),
        };

        debug!("Waiting for User Input from {:?}", menu);
        let usin = match self.rx.recv() {
            Ok(u) => u,
            Err(error) => {
                error!("Error while receiving User Input: {:?}", error);
                UserSelection::GoTo(Menu::Main)
            }
        };
        debug!("Proceeding after receiving User Input from {:?}", menu);
        usin
    }

    fn exit(&self, contents_changed: bool) -> UserSelection {
        debug!("Exiting rust-keylock...");
        if contents_changed {
            let _ = self.jvm.invoke_async(
                &self.show_menu,
                "apply",
                &vec![InvocationArg::from(Menu::Exit.get_name())],
                super::callbacks::ui_callback);
            let user_selection = self.rx.recv().unwrap();
            user_selection
        } else {
            UserSelection::GoTo(Menu::ForceExit)
        }
    }

    fn show_message(&self, message: &str, options: Vec<UserOption>, severity: MessageSeverity) -> UserSelection {
        debug!("Showing Message '{}'", message);
        let scala_user_options: Vec<ScalaUserOption> = options.iter()
            .clone()
            .map(|user_option| ScalaUserOption::new(user_option))
            .collect();
        let _ = self.jvm.invoke_async(
            &self.show_message,
            "apply",
            &vec![
                InvocationArg::from((
                    scala_user_options.as_slice(),
                    "org.rustkeylock.japi.ScalaUserOption",
                    &self.jvm)),
                InvocationArg::from(message),
                InvocationArg::from(severity.to_string())],
            super::callbacks::ui_callback);
        let user_selection = self.rx.recv().unwrap();
        user_selection
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub struct ScalaEntry {
    pub name: String,
    pub user: String,
    pub pass: String,
    pub desc: String,
}

impl ScalaEntry {
    fn new(entry: &Entry) -> ScalaEntry {
        ScalaEntry {
            name: entry.name.clone(),
            user: entry.user.clone(),
            pass: entry.pass.clone(),
            desc: entry.desc.clone(),
        }
    }

    fn empty() -> ScalaEntry {
        ScalaEntry {
            name: "".to_string(),
            user: "".to_string(),
            pass: "".to_string(),
            desc: "".to_string(),
        }
    }
}

#[derive(Serialize, Deserialize, Debug)]
pub struct ScalaUserOption {
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