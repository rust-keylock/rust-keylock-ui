extern crate fs_extra;

use std::{env, fs};
use std::error::Error;
use std::fmt;
use std::fs::File;
use std::path::{Path, PathBuf};

fn main() {
    let out_dir = env::var("OUT_DIR").unwrap();
    println!("cargo:rerun-if-changed=../scala/target/desktop-ui-0.8.1.jar");

    // Copy the needed jar files if they are available
    // (that is, if the build is done with the full source-code - not in crates.io)
    copy_jars_from_scala();
    let _ = copy_jars_to_exec_directory(&out_dir);
}

// Copies the jars from the `scala` directory to the source directory of rust.
fn copy_jars_from_scala() {
    // If the java directory exists, copy the generated jars in the `scalaassets` directory
    if File::open("../scala/target/desktop-ui-0.8.1.jar").is_ok() {
        let home = env::var("CARGO_MANIFEST_DIR").unwrap();
        let scalaassets_path_buf = Path::new(&home).join("scalaassets");
        let scalaassets_path = scalaassets_path_buf.to_str().unwrap().to_owned();

        let _ = fs_extra::remove_items(vec![scalaassets_path.clone()].as_ref());

        let _ = fs::create_dir_all(scalaassets_path_buf.clone())
            .map_err(|error| panic!("Cannot create dir '{:?}': {:?}", scalaassets_path_buf, error));

        let jar_source_path = "../scala/target/desktop-ui-0.8.1.jar";
        let lib_source_path = "../scala/target/lib";
        let ref options = fs_extra::dir::CopyOptions::new();
        let _ = fs_extra::copy_items(vec![lib_source_path, jar_source_path].as_ref(), scalaassets_path, options);
    }
}

// Copies the jars to and returns the PathBuf of the exec directory.
fn copy_jars_to_exec_directory(out_dir: &str) -> PathBuf {
    let mut exec_dir_path_buf = PathBuf::from(out_dir);
    exec_dir_path_buf.pop();
    exec_dir_path_buf.pop();
    exec_dir_path_buf.pop();

    let scalaassets_output = exec_dir_path_buf.clone();
    let scalaassets_output_dir = scalaassets_output.to_str().unwrap();


    let home = env::var("CARGO_MANIFEST_DIR").unwrap();
    let scalaassets_path_buf = Path::new(&home).join("scalaassets");
    let scalaassets_path = scalaassets_path_buf.to_str().unwrap().to_owned();

    let _ = fs_extra::remove_items(vec![format!("{}/scalaassets", scalaassets_output_dir)].as_ref());

    let ref options = fs_extra::dir::CopyOptions::new();
    let _ = fs_extra::copy_items(vec![scalaassets_path].as_ref(), scalaassets_output_dir, options);
    exec_dir_path_buf
}

#[derive(Debug)]
struct RklUiBuildError {
    description: String
}

impl fmt::Display for RklUiBuildError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.description)
    }
}

impl Error for RklUiBuildError {
    fn description(&self) -> &str {
        self.description.as_str()
    }
}

impl From<std::env::VarError> for RklUiBuildError {
    fn from(err: std::env::VarError) -> RklUiBuildError {
        RklUiBuildError { description: format!("{:?}", err) }
    }
}

impl From<std::io::Error> for RklUiBuildError {
    fn from(err: std::io::Error) -> RklUiBuildError {
        RklUiBuildError { description: format!("{:?}", err) }
    }
}
