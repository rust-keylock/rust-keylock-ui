#!/bin/bash

set -e

change_dir() {
    cd $1
    echo "Entered `pwd`"
}

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Entered Base directory $BASEDIR

cargo install cross # --force # force should be used in order to update

UI_RUST="$BASEDIR/rust"
UI_RUST_KEYLOCK_LINUX_x86_64="$UI_RUST/target/x86_64-unknown-linux-gnu/release"
UI_RUST_KEYLOCK_LINUX_x86_64_FINAL="$UI_RUST/target/x86_64-unknown-linux-gnu/rust-keylock"
UI_RUST_KEYLOCK_WIN_x86_64="$UI_RUST/target/x86_64-pc-windows-gnu/release"
UI_RUST_KEYLOCK_WIN_x86_64_FINAL="$UI_RUST/target/x86_64-pc-windows-gnu/rust-keylock"
UI_DEPLOY=$UI_RUST/target/deploy
mkdir -p $UI_DEPLOY

# Proceed with the build
change_dir $UI_RUST
echo "Building Java"
mvn clean install -f ../java/pom.xml

echo "Building rust-keylock-lib target for x86_64-unknown-linux-gnu"
# Define the libraries location in order to be downloaded and deployed by j4rs during rust-keylock-ui build.
export RKL_J4RS_INST_DIR=$UI_RUST_KEYLOCK_LINUX_x86_64_FINAL
# Do the build. Download all the jar dependencies needed (for windows too).These windows artifacts cannot be downloaded
# during cross compilation because j4rs cannot run, as it is being compiled for windows version, but in a Linux OS.
RKL_BUILD_MODE="all_java_artifacts" cargo build --target=x86_64-unknown-linux-gnu --release

echo "Building rust-keylock-lib target for x86_64-pc-windows-gnu"
# Do the build. Do not download any jar dependencies. They are downloaded during the Linux build above.
RKL_BUILD_MODE="no_java_artifacts" cross build --target=x86_64-pc-windows-gnu --release

# TMP_USER=$USER
# chown -R $TMP_USER:$TMP_USER $UI_RUST/target

echo "Copying the executable for Linux in $UI_RUST_KEYLOCK_LINUX_x86_64_FINAL"
cp $UI_RUST_KEYLOCK_LINUX_x86_64/rust-keylock-ui $UI_RUST_KEYLOCK_LINUX_x86_64_FINAL

echo "Copying the executable, the deps and the jassets for Windows in $UI_RUST_KEYLOCK_WIN_x86_64_FINAL"
mkdir -p $UI_RUST_KEYLOCK_WIN_x86_64_FINAL/deps
cp $UI_RUST_KEYLOCK_WIN_x86_64/rust-keylock-ui.exe $UI_RUST_KEYLOCK_WIN_x86_64_FINAL
cp $UI_RUST_KEYLOCK_WIN_x86_64/deps/j4rs*.dll $UI_RUST_KEYLOCK_WIN_x86_64_FINAL/deps
# The jars are located under the linux target because j4rs cannot install Maven artifacts during cross compilation builds.
# The cross compilation involves running a special docker image (via the cross crate).
cp -R $UI_RUST_KEYLOCK_LINUX_x86_64_FINAL/jassets $UI_RUST_KEYLOCK_WIN_x86_64_FINAL
rm $UI_RUST_KEYLOCK_WIN_x86_64_FINAL/jassets/*-linux.jar
# Remove the windows jars from the Linux jassets
rm $UI_RUST_KEYLOCK_LINUX_x86_64_FINAL/jassets/*-win.jar

change_dir $UI_RUST_KEYLOCK_LINUX_x86_64_FINAL/..
echo "Generating linux compressed file in `pwd`"
tar -zcf rust-keylock-ui-linux-x86_64.tar.gz rust-keylock
echo "Copying the compressed file in $UI_DEPLOY"
cp rust-keylock-ui-linux-x86_64.tar.gz $UI_DEPLOY

change_dir $UI_RUST_KEYLOCK_WIN_x86_64_FINAL/..
echo "Generating windows compressed file in `pwd`"
zip -r rust-keylock-ui-windows-x86_64.zip rust-keylock
echo "Copying the compressed file in $UI_DEPLOY"
cp rust-keylock-ui-windows-x86_64.zip $UI_DEPLOY

echo "Generating checksums"
cd $UI_DEPLOY
sha512sum rust-keylock-ui-linux-x86_64.tar.gz > sha512.txt
sha1sum rust-keylock-ui-linux-x86_64.tar.gz > sha1.txt
sha512sum rust-keylock-ui-windows-x86_64.zip >> sha512.txt
sha1sum rust-keylock-ui-windows-x86_64.zip >> sha1.txt

echo "Rust build for rust-keylock-ui completed."