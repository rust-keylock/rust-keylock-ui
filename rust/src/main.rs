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

#[macro_use]
extern crate log;
extern crate log4rs;
#[macro_use]
extern crate lazy_static;
extern crate rust_keylock;
extern crate serde;
extern crate serde_json;
#[macro_use]
extern crate serde_derive;
extern crate j4rs;

use j4rs::ClasspathEntry;
use std::sync::Mutex;
use std::sync::mpsc::{self, Sender, Receiver};
use rust_keylock::UserSelection;

mod ui_editor;
mod logger;
mod callbacks;
mod errors;

lazy_static! {
    static ref TX: Mutex<Option<Sender<UserSelection>>> = Mutex::new(None);
}

fn main() {
    logger::init().expect("Could not initialize logging");

    let mut default_classpath_entry = std::env::current_exe().unwrap();
    default_classpath_entry.pop();
    default_classpath_entry.push("scalaassets");
    default_classpath_entry.push("desktop-ui-0.6.0.jar");

    debug!("Starting the JVM");
    let jvm_res = j4rs::new_jvm(vec![
        ClasspathEntry::new(default_classpath_entry
            .to_str()
            .unwrap_or("./scalaassets/desktop-ui-0.6.0.jar"))],
                                Vec::new());

    let jvm = jvm_res.unwrap();

    let (tx, rx): (Sender<UserSelection>, Receiver<UserSelection>) = mpsc::channel();
    // Release the lock before calling the execute.
    // Execute will not return for the whole lifetime of the app, so the lock would be for ever acquired if was not explicitly released using the brackets.
    {
        let mut tx_opt = TX.lock().unwrap();
        *tx_opt = Some(tx);
    }

    debug!("Initializing the editor");
    let editor = ui_editor::new(jvm, rx);
    info!("TX Mutex initialized. Executing native rust_keylock!");
    rust_keylock::execute(&editor)
}
