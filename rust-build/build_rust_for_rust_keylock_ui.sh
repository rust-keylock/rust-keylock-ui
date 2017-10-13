#!/bin/bash

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Entered Base directory $BASEDIR

UI_RUST="$BASEDIR/rust"
UI_RUST_KEYLOCK_LIB="$UI_RUST/target/release/librustkeylockui.so"
UI_SCALA_NATIVE="$BASEDIR/scala/src/main/resources/linux-x86-64/"
mkdir -p $UI_SCALA_NATIVE
cd $UI_RUST
cargo build --release

echo "Copying $UI_RUST_KEYLOCK_LIB to $UI_SCALA_NATIVE"
cp $UI_RUST_KEYLOCK_LIB $UI_SCALA_NATIVE

echo "Rust build for rust-keylock-android completed."
