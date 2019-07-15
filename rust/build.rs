use std::env;
use std::error::Error;
use std::fs::{self, File};
use std::path::{MAIN_SEPARATOR, Path};

use j4rs;
use j4rs::{Jvm, JvmBuilder, LocalJarArtifact, MavenArtifact};

fn main() {
    let ui_jar = "desktop-ui-0.9.0.jar";
    let desktop_ui_jar_in_scala_target = format!("../scala/target/{}", ui_jar);
    println!("cargo:rerun-if-changed={}", desktop_ui_jar_in_scala_target);

    // The target os is needed for the classifiers of javafx dependencies
    let target_os = match env::var("CARGO_CFG_TARGET_OS").unwrap_or("linux".to_string()).as_ref() {
        "macos" => "mac".to_string(),
        "windows" => "win".to_string(),
        _ => "linux".to_string()
    };

    // If the scala target directory exists, copy the desktop-ui jar to rust
    copy_from_scala(&desktop_ui_jar_in_scala_target);

    // The jassets are located inside the default rust-keylock location
    let mut j4rs_installation_path_buf = rust_keylock::default_rustkeylock_location();
    j4rs_installation_path_buf.push("lib");
    fs::create_dir_all(&j4rs_installation_path_buf).unwrap();

    let j4rs_installation_path = j4rs_installation_path_buf.to_str().unwrap();

    Jvm::copy_j4rs_libs_under(j4rs_installation_path).unwrap();

    let jvm = JvmBuilder::new()
        .with_base_path(j4rs_installation_path)
        .build()
        .unwrap();

    // Deploy the desktop-ui jar
    let home = env::var("CARGO_MANIFEST_DIR").unwrap();
    let scalaassets_path_buf = Path::new(&home).join("scalaassets");
    let scalaassets_path = scalaassets_path_buf.to_str().unwrap().to_owned();
    let artf1 = LocalJarArtifact::new(&format!("{}{}{}", scalaassets_path, MAIN_SEPARATOR, ui_jar));
    jvm.deploy_artifact(&artf1).unwrap();

    // Deploy from Maven
    println!("cargo:warning=Downloading Maven dependencies... This may take a while the first time you build.");
    maven("org.openjfx:javafx-base:11.0.2", &jvm);
    maven(&format!("org.openjfx:javafx-base:11.0.2:{}", target_os), &jvm);
    maven("org.openjfx:javafx-controls:11.0.2", &jvm);
    maven(&format!("org.openjfx:javafx-controls:11.0.2:{}", target_os), &jvm);
    maven("org.openjfx:javafx-graphics:11.0.2", &jvm);
    maven(&format!("org.openjfx:javafx-graphics:11.0.2:{}", target_os), &jvm);
    maven("org.openjfx:javafx-media:11.0.2", &jvm);
    maven(&format!("org.openjfx:javafx-media:11.0.2:{}", target_os), &jvm);
    maven("org.scala-lang:scala-library:2.12.8", &jvm);
    maven("org.scala-lang:scala-reflect:2.12.7", &jvm);
    maven("org.scala-stm:scala-stm_2.12:0.8", &jvm);
    maven("org.scalafx:scalafx_2.12:11-R16", &jvm);
    maven("com.typesafe.scala-logging:scala-logging_2.12:3.9.0", &jvm);
    println!("cargo:warning=Maven dependencies downloaded!");
}

fn maven(s: &str, jvm: &Jvm) {
    let artifact = MavenArtifact::from(s);
    let _ = jvm.deploy_artifact(&artifact).map_err(|error| {
        println!("cargo:warning=Could not download Maven artifact {}: {}", s, error.description());
    });
}

fn copy_from_scala(desktop_ui_jar_in_scala_target: &str) {
    if File::open(desktop_ui_jar_in_scala_target).is_ok() {
        let home = env::var("CARGO_MANIFEST_DIR").unwrap();
        let scalaassets_path_buf = Path::new(&home).join("scalaassets");
        let scalaassets_path = scalaassets_path_buf.to_str().unwrap().to_owned();

        let _ = fs_extra::remove_items(vec![scalaassets_path.clone()].as_ref());

        let _ = fs::create_dir_all(scalaassets_path_buf.clone())
            .map_err(|error| panic!("Cannot create dir '{:?}': {:?}", scalaassets_path_buf, error));

        let jar_source_path = desktop_ui_jar_in_scala_target;
        let ref options = fs_extra::dir::CopyOptions::new();
        let _ = fs_extra::copy_items(vec![jar_source_path].as_ref(), scalaassets_path, options);
    }
}
