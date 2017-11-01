#!/bin/bash

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`

cargo install cross --force
sh build-scripts/build_rust_for_rust_keylock_ui.sh

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`

mvn -f scala/pom.xml clean install
