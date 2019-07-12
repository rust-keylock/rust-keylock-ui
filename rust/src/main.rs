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

use std::env;

use j4rs::JavaOpt;
use log::*;

mod ui_editor;
mod logger;
mod callbacks;
mod errors;

fn main() {
    logger::init().expect("Could not initialize logging ");

    let jh = env::var("RUST_KEYLOCK_UI_JAVA_USER_HOME")
        .map(|s| format!("-Duser.home={}", s))
        .unwrap_or("".to_string());

    let jopts: Vec<JavaOpt> = if jh.is_empty() {
        Vec::new()
    } else {
        vec![JavaOpt::new(&jh)]
    };

    debug!("Starting the JVM");
    // The jassets are located inside the default rust-keylock location
    let mut j4rs_installation_path_buf = rust_keylock::default_rustkeylock_location();
    j4rs_installation_path_buf.push("etc");
    j4rs_installation_path_buf.push("lib");

    let j4rs_installation_path = j4rs_installation_path_buf.to_str().unwrap();

    let jvm_res = j4rs::JvmBuilder::new()
        .java_opts(jopts)
        .with_base_path(j4rs_installation_path)
        .build();

    let jvm = jvm_res.unwrap();
    debug!("Initializing the editor");
    let editor = ui_editor::new(jvm);
    info!("Executing native rust_keylock!");
    rust_keylock::execute_async(Box::new(editor))
}
