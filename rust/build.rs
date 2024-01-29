use std::env;
use std::fs::{self, File};
use std::path::{Path, MAIN_SEPARATOR};

use j4rs;
use j4rs::{Jvm, JvmBuilder, LocalJarArtifact, MavenArtifact};

fn main() {
    let ui_jar = "rust-keylock-ui-java-0.15.0.jar";
    let desktop_ui_jar_in_java_target = format!("../java/target/{}", ui_jar);
    println!("cargo:rerun-if-changed={}", desktop_ui_jar_in_java_target);

    // The target os is needed for the classifiers of javafx dependencies
    let target_os = match env::var("CARGO_CFG_TARGET_OS").unwrap_or("linux".to_string()).as_ref() {
        "macos" => "mac".to_string(),
        "windows" => "win".to_string(),
        _ => "linux".to_string(),
    };

    // If the java target directory exists, copy the desktop-ui jar to rust
    copy_from_java(&desktop_ui_jar_in_java_target);

    let j4rs_installation_path = match env::var("RKL_J4RS_INST_DIR") {
        Ok(path) => path,
        Err(_) => {
            let mut j4rs_installation_path_buf = rust_keylock::default_rustkeylock_location();
            j4rs_installation_path_buf.push("lib");

            j4rs_installation_path_buf.to_str().unwrap().to_owned()
        }
    };

    fs::create_dir_all(&j4rs_installation_path).unwrap();

    let _ = fs_extra::remove_items(vec![&j4rs_installation_path].as_ref());

    Jvm::copy_j4rs_libs_under(&j4rs_installation_path).unwrap();

    match env::var("RKL_BUILD_MODE").unwrap_or("default".to_string()).as_ref() {
        "no_java_artifacts" => {
            println!("cargo:warning=Will not download any Java artifacts");
        }
        "all_java_artifacts" => {
            println!("cargo:warning=Downloading all Java artifacts... This may take a while the first time you build.");
            let jvm = JvmBuilder::new().with_base_path(&j4rs_installation_path).build().unwrap();
            deploy_desktop_ui_jar(&jvm, ui_jar);
            deploy_os_jars(&jvm, "win");
            deploy_os_jars(&jvm, "linux");
        }
        _ => {
            println!("cargo:warning=Downloading Java artifacts for target host... This may take a while the first time you build.");
            let jvm = JvmBuilder::new().with_base_path(&j4rs_installation_path).build().unwrap();
            deploy_desktop_ui_jar(&jvm, ui_jar);
            deploy_os_jars(&jvm, &target_os);
        }
    };

    println!("cargo:warning=Artifacts provisioned successfully.");
}

fn deploy_os_jars(jvm: &Jvm, target_os: &str) {
    maven("org.openjfx:javafx-base:22-ea+16", &jvm);
    maven(&format!("org.openjfx:javafx-base:22-ea+16:{}", target_os), &jvm);
    maven("org.openjfx:javafx-controls:22-ea+16", &jvm);
    maven(&format!("org.openjfx:javafx-controls:22-ea+16:{}", target_os), &jvm);
    maven("org.openjfx:javafx-fxml:22-ea+16", &jvm);
    maven(&format!("org.openjfx:javafx-fxml:22-ea+16:{}", target_os), &jvm);
    maven("org.openjfx:javafx-graphics:22-ea+16", &jvm);
    maven(&format!("org.openjfx:javafx-graphics:22-ea+16:{}", target_os), &jvm);
    maven("org.openjfx:javafx-media:22-ea+16", &jvm);
    maven(&format!("org.openjfx:javafx-media:22-ea+16:{}", target_os), &jvm);
    maven("org.openjfx:javafx-swing:22-ea+16", &jvm);
    maven(&format!("org.openjfx:javafx-swing:22-ea+16:{}", target_os), &jvm);
    maven("org.slf4j:slf4j-api:2.0.11", &jvm);
    maven("ch.qos.logback:logback-core:1.4.12", &jvm);
    maven("ch.qos.logback:logback-classic:1.4.12", &jvm);
}

fn deploy_desktop_ui_jar(jvm: &Jvm, jar: &str) {
    // Deploy the desktop-ui jar
    let home = env::var("CARGO_MANIFEST_DIR").unwrap();
    let javaassets_path_buf = Path::new(&home).join("javaassets");
    let javaassets_path = javaassets_path_buf.to_str().unwrap().to_owned();
    let artf1 = LocalJarArtifact::new(&format!("{}{}{}", javaassets_path, MAIN_SEPARATOR, jar));
    jvm.deploy_artifact(&artf1).unwrap();
}

fn maven(s: &str, jvm: &Jvm) {
    let artifact = MavenArtifact::from(s);
    let _ = jvm.deploy_artifact(&artifact).map_err(|error| {
        println!("cargo:warning=Could not download Maven artifact {}: {:?}", s, error);
    });
}

fn copy_from_java(desktop_ui_jar_in_java_target: &str) {
    if File::open(desktop_ui_jar_in_java_target).is_ok() {
        let home = env::var("CARGO_MANIFEST_DIR").unwrap();
        let javaassets_path_buf = Path::new(&home).join("javaassets");
        let javaassets_path = javaassets_path_buf.to_str().unwrap().to_owned();

        let _ = fs_extra::remove_items(vec![javaassets_path.clone()].as_ref());

        let _ = fs::create_dir_all(javaassets_path_buf.clone())
            .map_err(|error| panic!("Cannot create dir '{:?}': {:?}", javaassets_path_buf, error));

        let jar_source_path = desktop_ui_jar_in_java_target;
        let ref options = fs_extra::dir::CopyOptions::new();
        let _ = fs_extra::copy_items(vec![jar_source_path].as_ref(), javaassets_path, options);
    }
}
