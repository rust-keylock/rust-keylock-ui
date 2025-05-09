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

use std::env;

use j4rs::JavaOpt;
use log::*;
use std::path::PathBuf;

mod callbacks;
mod errors;
mod logger;
mod ui_editor;

#[tokio::main]
async fn main() -> errors::Result<()> {
    logger::init().expect("Could not initialize logging ");
    let jh = env::var("RUST_KEYLOCK_UI_JAVA_USER_HOME")
        .map(|s| format!("-Duser.home={}", s))
        .unwrap_or("".to_string());

    debug!("Starting the JVM");

    // Set the desired j4rs installation directory.
    // In a snaps environment, this is under the $SNAP/opt/j4rs.
    // In other environments, it is under the default rust-keylock location (in the home directory)
    let mut j4rs_installation_path = match env::var("RKL_J4RS_INST_DIR") {
        Ok(path) => PathBuf::from(path),
        Err(_) => {
            let mut j4rs_installation_path_buf = rust_keylock::default_rustkeylock_location();
            j4rs_installation_path_buf.push("lib");

            j4rs_installation_path_buf
        }
    };

    let base_path_string = j4rs_installation_path.to_str().unwrap().to_owned();

    j4rs_installation_path.push("jassets");
    let jassets_path_string = j4rs_installation_path.to_str().unwrap().to_owned();

    let mut jopts: Vec<JavaOpt> = if jh.is_empty() { Vec::new() } else { vec![JavaOpt::new(&jh)] };

    let modules_path = format!("--module-path {}", jassets_path_string);
    jopts.push(JavaOpt::new(&modules_path));
    jopts.push(JavaOpt::new("--add-modules javafx.base,javafx.controls,javafx.graphics,javafx.fxml"));

    let jvm_res = j4rs::JvmBuilder::new().java_opts(jopts).with_base_path(&base_path_string).build();

    let jvm = jvm_res.unwrap();
    debug!("Initializing the editor");
    let editor = ui_editor::new(&jvm)?;
    info!("Executing native rust_keylock!");
    rust_keylock::execute_async(Box::new(editor)).await;
    Ok(())
}
