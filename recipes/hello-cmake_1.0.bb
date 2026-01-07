# hello-cmake_1.0.bb - CMake-based BitBake recipe
#
# This recipe demonstrates:
# - CMake build system integration in Yocto
# - Out-of-tree builds with B != S
# - CMake variable passing from BitBake to CMake
# - Cross-compilation setup for embedded targets
# - Modern C++ application packaging
#
# Learning objectives:
# 1. Understand cmake class and its automatic task implementations
# 2. Learn how to pass BitBake variables to CMake via EXTRA_OECMAKE
# 3. Practice out-of-tree builds (separate source and build directories)
# 4. Handle C++ dependencies and compiler flags

SUMMARY = "Hello World application built with CMake"
DESCRIPTION = "Educational example demonstrating CMake integration in Yocto. \
This recipe shows how to build modern C++ applications using CMake, pass \
configuration options, and handle cross-compilation toolchain setup automatically."

HOMEPAGE = "https://github.com/example/hello-cmake"
SECTION = "examples"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e3fc50a88d0a364313df4b21ef20c29e"

# Source location
SRC_URI = "git://github.com/example/hello-cmake.git;protocol=https;branch=main"
SRCREV = "abcdef1234567890abcdef1234567890abcdef12"

S = "${WORKDIR}/git"

# Inherit cmake class - provides:
# - do_configure: runs cmake with cross-compile toolchain file
# - do_compile: runs cmake --build (equivalent to make)
# - do_install: runs cmake --install (equivalent to make install)
inherit cmake

# EXTRA_OECMAKE: CMake arguments passed to cmake command
EXTRA_OECMAKE = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_TESTING=OFF \
    -DBUILD_EXAMPLES=ON \
    -DENABLE_OPTIMIZATION=ON \
    -DCMAKE_VERBOSE_MAKEFILE=ON \
"

# Build dependencies
DEPENDS = " \
    libstdc++ \
    zlib \
"

# Runtime dependencies
RDEPENDS:${PN} = " \
    libstdc++ \
"

# Custom configuration hook - runs before cmake
do_configure:prepend() {
    # Generate version header from BitBake variables
    echo "#define VERSION \"${PV}\"" > ${S}/include/version.h
    echo "#define BUILD_DATE \"$(date +'%Y-%m-%d')\"" >> ${S}/include/version.h
}

# Custom install hook - runs after installation
do_install:append() {
    # Install additional configuration files
    install -d ${D}${sysconfdir}/hello-cmake
    echo "# Hello CMake Configuration" > ${D}${sysconfdir}/hello-cmake/config.ini
    echo "log_level=INFO" >> ${D}${sysconfdir}/hello-cmake/config.ini

    # Create runtime directory
    install -d ${D}${localstatedir}/lib/hello-cmake
}

# Package file organization
FILES:${PN} = " \
    ${bindir}/hello-cmake \
    ${sysconfdir}/hello-cmake \
    ${localstatedir}/lib/hello-cmake \
"

FILES:${PN}-dev = " \
    ${includedir}/hello-cmake \
    ${libdir}/cmake/HelloCMake \
    ${libdir}/pkgconfig/hello-cmake.pc \
"

FILES:${PN}-dbg = " \
    ${bindir}/.debug \
    ${libdir}/.debug \
"
