# The main functionality is retrieved from the `cross` project (https://github.com/rust-embedded/cross)
FROM ubuntu:18.04

MAINTAINER astonbitecode <astonbitecode@gmail.com>

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ca-certificates \
    cmake \
    gcc \
    libc6-dev \
    make \
    pkg-config

COPY xargo.sh /
RUN bash /xargo.sh

RUN dpkg --add-architecture i386 && apt-get update && \
    apt-get install -y --no-install-recommends \
        wine-stable \
        wine64 \
        wine32 \
        libz-mingw-w64-dev

RUN apt-get install -y --no-install-recommends g++-mingw-w64-x86-64

# run-detectors are responsible for calling the correct interpreter for exe
# files. For some reason it does not work inside a docker container (it works
# fine in the host). So we replace the usual paths of run-detectors to run wine
# directly. This only affects the guest, we are not messing up with the host.
#
# See /usr/share/doc/binfmt-support/detectors
RUN mkdir -p /usr/lib/binfmt-support/ && \
    rm -f /usr/lib/binfmt-support/run-detectors /usr/bin/run-detectors && \
    ln -s /usr/bin/wine /usr/lib/binfmt-support/run-detectors && \
    ln -s /usr/bin/wine /usr/bin/run-detectors

COPY windows-entry.sh /
ENTRYPOINT ["/windows-entry.sh"]

ENV CARGO_TARGET_X86_64_PC_WINDOWS_GNU_LINKER=x86_64-w64-mingw32-gcc \
    CARGO_TARGET_X86_64_PC_WINDOWS_GNU_RUNNER=wine \
    CC_x86_64_pc_windows_gnu=x86_64-w64-mingw32-gcc-posix \
CXX_x86_64_pc_windows_gnu=x86_64-w64-mingw32-g++-posix

# Download and copy the openjdk package for windows
RUN apt-get install -y --no-install-recommends unzip curl
RUN bash -c 'cd /tmp && curl -O https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_windows-x64_bin.zip'
RUN bash -c 'mkdir -p /usr/lib/jvm/openjdk-11.0.1_windows-x64'
RUN bash -c 'unzip /tmp/openjdk-11.0.1_windows-x64_bin.zip -d /usr/lib/jvm/openjdk-11.0.1_windows-x64'
RUN bash -c 'rm /tmp/openjdk-11.0.1_windows-x64_bin.zip'

ENV JAVA_HOME="/usr/lib/jvm/openjdk-11.0.1_windows-x64/jdk-11.0.1"