___rust-keylock-ui___ is the [Editor](https://rust-keylock.github.io/rust-keylock-lib/rust_keylock/trait.Editor.html) that manages the [rust-keylock-lib](https://github.com/rust-keylock/rust-keylock-lib), providing a User Interface using JavaFX (via [ScalaFX](http://www.scalafx.org)).

![Build Status](https://travis-ci.org/rust-keylock/rust-keylock-ui.svg?branch=master)

## General

___rust-keylock___ is a password manager and its goals are to be:

* Secure
* Simple to use
* Portable
* Extensible

The core logic is written in [Rust](https://www.rust-lang.org), but the presentation/User interaction parts are in different languages.

## Features

### Security

 * The data is locked with a user-defined master password, using _bcrypt_ password hashing
 * Encryption using _AES_ with _CTR_ mode
 * Data integrity checks with SHA3 (Keccak)
 * Encrypted bytes blending
 * Passwords are kept encrypted in memory
 * Encryption keys on runtime are stored in safe, non-swappable memory
 * Encryption keys change upon saving, even if the user master password remains the same. This results to different encrypted products, even if the data that is being encrypted is the same.
 
### Application Portability

 * [Shell implementation](https://github.com/rust-keylock/rust-keylock-shell) running on Linux and Windows
 * [JavaFX implementation](https://github.com/rust-keylock/rust-keylock-ui) running on Linux and Windows
 * [Android implementation](https://github.com/rust-keylock/rust-keylock-android) soon to be published in [F-Droid](https://gitlab.com/fdroid/fdroiddata/merge_requests/2141)

Thanks to [xargo](https://github.com/japaric/xargo), [cross](https://github.com/japaric/cross) and [JNA](https://github.com/java-native-access/jna)!
 
### Import/export mechanism

 * Export/import encrypted passwords to/from the filesystem

## Download and Install

Instructions can be found [here](https://rust-keylock.github.io/download/rkl/).

## Screenshots

![rkl1](gh-images/rkl1.png)

![rkl2](gh-images/rkl2.png)

![rkl3](gh-images/rkl3.png)

![rkl4](gh-images/rkl4.png)
