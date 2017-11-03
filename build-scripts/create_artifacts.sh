#!/bin/bash

if [ -n "${TRAVIS_BUILD_DIR}" ]; then
	WORKING_DIR=${TRAVIS_BUILD_DIR}
else
	WORKING_DIR=$(dirname "$0")
	cd $WORKING_DIR/../
	WORKING_DIR=`pwd`
fi

cd $WORKING_DIR/scala/target/

tar -zcvf rust-keylock-ui.tar.gz lib desktop-ui-*.jar
zip -r rust-keylock-ui.zip lib desktop-ui-*.jar

sha512sum rust-keylock-ui.tar.gz > sha512.txt
sha512sum rust-keylock-ui.zip >> sha512.txt

sha1sum rust-keylock-ui.tar.gz > sha1.txt
sha1sum rust-keylock-ui.zip >> sha1.txt
