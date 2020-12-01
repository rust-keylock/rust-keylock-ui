#!/bin/bash

set -e

# Update the PKGBUILD (usually pkgver and sha512sums) and then update the .SRCINFO with the following
makepkg --printsrcinfo > .SRCINFO

# Build with sync deps
makepkg --syncdeps

# Install
makepkg --install