#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Entered Base directory $BASEDIR

UI_RUST="$BASEDIR/rust"
cd $UI_RUST

echo "Building rust-keylock-lib target for x86_64-unknown-linux-gnu"
cargo build
cargo test

echo "Rust build for rust-keylock-ui completed."
