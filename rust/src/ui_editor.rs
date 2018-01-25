use rust_keylock::{Editor, UserSelection, Menu, Safe, UserOption, MessageSeverity, RklConfiguration};
use super::{StringCallback, StringListCallback, ShowEntryCallback, ShowEntriesSetCallback, LogCallback, logger, ScalaEntriesSet,
            ScalaEntry, ShowMessageCallback, ScalaUserOptionsSet, StringList};
use std::sync::mpsc::Receiver;

pub struct AndroidImpl {
    show_menu_cb: StringCallback,
    show_entry_cb: ShowEntryCallback,
    show_entries_set_cb: ShowEntriesSetCallback,
    show_message_cb: ShowMessageCallback,
    edit_configuration_cb: StringListCallback,
    rx: Receiver<UserSelection>,
}

pub fn new(show_menu_cb: StringCallback,
           show_entry_cb: ShowEntryCallback,
           show_entries_set_cb: ShowEntriesSetCallback,
           show_message_cb: ShowMessageCallback,
           edit_configuration_cb: StringListCallback,
           log_cb: LogCallback,
           rx: Receiver<UserSelection>)
           -> AndroidImpl {

    // Initialize the Android logger
    logger::init(log_cb);
    // Return the Editor
    AndroidImpl {
        show_menu_cb: show_menu_cb,
        show_entry_cb: show_entry_cb,
        show_entries_set_cb: show_entries_set_cb,
        show_message_cb: show_message_cb,
        edit_configuration_cb: edit_configuration_cb,
        rx: rx,
    }
}

impl Editor for AndroidImpl {
    fn show_password_enter(&self) -> UserSelection {
        debug!("Opening the password fragment");
        let try_pass_menu_name = Menu::TryPass.get_name();
        (self.show_menu_cb)(super::to_java_string(try_pass_menu_name));
        debug!("Waiting for password...");
        let user_selection = self.rx.recv().unwrap();
        user_selection
    }

    fn show_change_password(&self) -> UserSelection {
        debug!("Opening the change password fragment");
        let change_pass_menu_name = Menu::ChangePass.get_name();
        (self.show_menu_cb)(super::to_java_string(change_pass_menu_name));
        debug!("Waiting for password...");
        let user_selection = self.rx.recv().unwrap();
        user_selection
    }

    fn show_menu(&self, menu: &Menu, safe: &Safe, configuration: &RklConfiguration) -> UserSelection {
        debug!("Opening menu '{:?}' with entries size {}", menu, safe.get_entries().len());

        match menu {
            &Menu::Main => (self.show_menu_cb)(super::to_java_string(Menu::Main.get_name())),
            &Menu::EntriesList(_) => {
                let scala_entries_set = if safe.get_entries().len() == 0 {
                    ScalaEntriesSet::with_nulls()
                } else {
                    ScalaEntriesSet::from(safe.get_entries())
                };
                let filter_ptr = if safe.get_filter().len() == 0 {
                    super::to_java_string("null".to_string())
                } else {
                    super::to_java_string(safe.get_filter().clone())
                };
                (self.show_entries_set_cb)(Box::new(scala_entries_set), filter_ptr);
            }
            &Menu::ShowEntry(index) => {
                let entry = safe.get_entry_decrypted(index);
                (self.show_entry_cb)(Box::new(ScalaEntry::new(&entry)), index as i32, false, false);
            }
            &Menu::DeleteEntry(index) => {
                let ref entry = safe.get_entry(index);
                (self.show_entry_cb)(Box::new(ScalaEntry::new(&entry)), index as i32, false, true);
            }
            &Menu::NewEntry => {
                let empty_entry = ScalaEntry::empty();
                // In order to denote that this is a new entry, put -1 as index
                (self.show_entry_cb)(Box::new(empty_entry), -1, true, false);
            }
            &Menu::EditEntry(index) => {
                let ref selected_entry = safe.get_entry_decrypted(index);
                (self.show_entry_cb)(Box::new(ScalaEntry::new(selected_entry)), index as i32, true, false);
            }
            &Menu::ExportEntries => (self.show_menu_cb)(super::to_java_string(Menu::ExportEntries.get_name())),
            &Menu::ImportEntries => (self.show_menu_cb)(super::to_java_string(Menu::ImportEntries.get_name())),
            &Menu::ShowConfiguration => {
                let conf_strings = vec![configuration.nextcloud.server_url.clone(),
                                        configuration.nextcloud.username.clone(),
                                        configuration.nextcloud.decrypted_password().unwrap(),
                                        configuration.nextcloud.use_self_signed_certificate.to_string()];
                (self.edit_configuration_cb)(Box::new(StringList::from(conf_strings)))
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
            let menu_name = Menu::Exit.get_name();
            (self.show_menu_cb)(super::to_java_string(menu_name));
            let user_selection = self.rx.recv().unwrap();
            user_selection
        } else {
            UserSelection::GoTo(Menu::ForceExit)
        }
    }

    fn show_message(&self, message: &str, options: Vec<UserOption>, severity: MessageSeverity) -> UserSelection {
        debug!("Showing Message '{}'", message);
        let scala_options_set = if options.len() == 0 {
            ScalaUserOptionsSet::with_nulls()
        } else {
            ScalaUserOptionsSet::from(&options[..])
        };
        (self.show_message_cb)(Box::new(scala_options_set),
                               super::to_java_string(message.to_string()),
                               super::to_java_string(severity.to_string()));
        let user_selection = self.rx.recv().unwrap();
        user_selection
    }
}
