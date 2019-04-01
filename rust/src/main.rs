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

extern crate dirs;
extern crate j4rs;
extern crate log;
extern crate log4rs;
extern crate rust_keylock;
extern crate serde;
extern crate serde_derive;
extern crate serde_json;

use j4rs::ClasspathEntry;
use log::*;

mod ui_editor;
mod logger;
mod callbacks;
mod errors;

fn main() {
    logger::init().expect("Could not initialize logging");

    let mut default_classpath_entry = std::env::current_exe().unwrap();
    default_classpath_entry.pop();
    default_classpath_entry.push("scalaassets");
    default_classpath_entry.push("desktop-ui-0.8.2.jar");

    debug!("Starting the JVM");
    let jvm_res = j4rs::JvmBuilder::new()
        .classpath_entry(ClasspathEntry::new(default_classpath_entry
            .to_str()
            .unwrap_or("./scalaassets/desktop-ui-0.8.2.jar")))
        .build();

    let jvm = jvm_res.unwrap();
    debug!("Initializing the editor");
    let editor = ui_editor::new(jvm);
    info!("Executing native rust_keylock!");
    rust_keylock::execute_async(Box::new(editor))
}
