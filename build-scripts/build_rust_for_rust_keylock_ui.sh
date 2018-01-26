#!/bin/bash

set -e

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

# Get and build the openssl

mkdir $BASEDIR/tools
cd $BASEDIR/tools

curl -O https://www.openssl.org/source/openssl-1.1.0g.tar.gz
tar xzf openssl-1.1.0g.tar.gz
OPENSSL_SRC_DIR=$BASEDIR/tools/openssl-1.1.0g

cd $OPENSSL_SRC_DIR

./config shared no-ssl2 no-ssl3 no-comp no-hw no-engine --openssldir=$OPENSSL_SRC_DIR/build --prefix=$OPENSSL_SRC_DIR/build
make all
make install

# Proceed with the build
cd $UI_RUST

echo "Building rust-keylock-lib target for x86_64-unknown-linux-gnu"
# The openssl is statically linked for the linux environments (It is needed by the native-tls crate and we should not expect from the target hosts to have the exact openssl version installed)
OPENSSL_DIR=$OPENSSL_SRC_DIR/build OPENSSL_LIB_DIR=$OPENSSL_SRC_DIR/build/lib OPENSSL_INCLUDE_DIR=$OPENSSL_SRC_DIR/build/include OPENSSL_STATIC=true cargo build --target=x86_64-unknown-linux-gnu --release

echo "Building rust-keylock-lib target for x86_64-pc-windows-gnu"
cross build --target=x86_64-pc-windows-gnu --release

echo "Copying $UI_RUST_KEYLOCK_LIB_LINUX_x86_64 to $UI_SCALA_NATIVE_LINUX_x86_64"
cp $UI_RUST_KEYLOCK_LIB_LINUX_x86_64 $UI_SCALA_NATIVE_LINUX_x86_64

echo "Copying $UI_RUST_KEYLOCK_LIB_WIN_x86_64 to $UI_SCALA_NATIVE_WIN_x86_64"
cp $UI_RUST_KEYLOCK_LIB_WIN_x86_64 $UI_SCALA_NATIVE_WIN_x86_64

echo "Rust build for rust-keylock-ui completed."
