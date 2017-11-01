#!/bin/bash

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Entered Base directory $BASEDIR

cargo install cross

UI_RUST="$BASEDIR/rust"
UI_RUST_KEYLOCK_LIB_LINUX_x86_64="$UI_RUST/target/x86_64-unknown-linux-gnu/release/librustkeylockui.so"
UI_RUST_KEYLOCK_LIB_WIN_x86_64="$UI_RUST/target/x86_64-pc-windows-gnu/release/rustkeylockui.dll"

UI_SCALA_NATIVE_LINUX_x86_64="$BASEDIR/scala/src/main/resources/linux-x86-64/"
mkdir -p $UI_SCALA_NATIVE_LINUX_x86_64
UI_SCALA_NATIVE_WIN_x86_64="$BASEDIR/scala/src/main/resources/win32-x86-64/"
mkdir -p $UI_SCALA_NATIVE_WIN_x86_64

cd $UI_RUST

echo "Building rust-keylock-lib target for x86_64-unknown-linux-gnu"
cross build --target=x86_64-unknown-linux-gnu --release

echo "Building rust-keylock-lib target for x86_64-pc-windows-gnu"
cross build --target=x86_64-pc-windows-gnu --release

echo "Copying $UI_RUST_KEYLOCK_LIB_LINUX_x86_64 to $UI_SCALA_NATIVE_LINUX_x86_64"
cp $UI_RUST_KEYLOCK_LIB_LINUX_x86_64 $UI_SCALA_NATIVE_LINUX_x86_64

echo "Copying $UI_RUST_KEYLOCK_LIB_WIN_x86_64 to $UI_SCALA_NATIVE_WIN_x86_64"
cp $UI_RUST_KEYLOCK_LIB_WIN_x86_64 $UI_SCALA_NATIVE_WIN_x86_64

echo "Rust build for rust-keylock-ui completed."

