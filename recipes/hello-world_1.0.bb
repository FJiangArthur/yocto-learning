# hello-world_1.0.bb - Simplest BitBake recipe demonstrating autotools-based build
#
# This recipe demonstrates:
# - Basic recipe structure and required variables
# - Autotools build system integration (./configure && make && make install)
# - Proper licensing and checksum verification
# - Source fetching from Git repository
# - Package installation into target image
#
# Learning objectives:
# 1. Understand minimal recipe requirements (SUMMARY, LICENSE, SRC_URI)
# 2. Learn how to inherit autotools class for automatic build
# 3. Practice proper license file checksum management
# 4. Understand do_compile() and do_install() tasks

SUMMARY = "Simple Hello World application built with autotools"
DESCRIPTION = "Educational example demonstrating the simplest BitBake recipe \
structure using the autotools build system. This recipe fetches source code \
from a Git repository, builds it using ./configure && make, and installs \
the resulting binary to the target image."

# Homepage and source code location for documentation
HOMEPAGE = "https://github.com/example/hello-world"
BUGTRACKER = "https://github.com/example/hello-world/issues"

# Section categorization for package management
SECTION = "examples"
PRIORITY = "optional"

# License specification - ALWAYS required in Yocto recipes
# Common licenses: MIT, GPLv2, Apache-2.0, BSD-3-Clause
LICENSE = "MIT"

# License file checksum - ensures license hasn't changed
# Generate with: md5sum ${S}/LICENSE
# This prevents silent license changes that could affect distribution
LIC_FILES_CHKSUM = "file://LICENSE;md5=d3e7c1e4a7e5d8e7f1e3c2d1e4a7b5c8"

# Source URI - where to fetch the source code
# Supported protocols: git://, http://, https://, ftp://, file://
# Git syntax: git://server/repo;protocol=https;branch=master
SRC_URI = "git://github.com/example/hello-world.git;protocol=https;branch=main"

# Git revision to checkout - use specific commit for reproducibility
# In production, NEVER use ${AUTOREV} - always pin to specific commits
SRCREV = "1234567890abcdef1234567890abcdef12345678"

# Alternative: for tarballs
# SRC_URI = "https://example.com/hello-world-${PV}.tar.gz"
# SRC_URI[md5sum] = "abcdef1234567890abcdef1234567890"
# SRC_URI[sha256sum] = "1234567890abcdef..."

# PV = Package Version (inherited from recipe filename _1.0.bb)
# PR = Package Revision (increment when recipe changes, not source)
PR = "r0"

# S = Source directory where unpacked sources are located
# Default is ${WORKDIR}/${BPN}-${PV}
# For Git repos, typically ${WORKDIR}/git
S = "${WORKDIR}/git"

# Inherit autotools class - provides automatic implementation of:
# - do_configure: runs ./configure with appropriate cross-compile flags
# - do_compile: runs make with parallel build jobs
# - do_install: runs make install DESTDIR=${D}
inherit autotools

# EXTRA_OECONF: Additional arguments passed to ./configure
# Common options:
# --disable-static: don't build static libraries
# --enable-shared: build shared libraries
# --with-feature: enable optional feature
EXTRA_OECONF = "--disable-static --enable-shared"

# EXTRA_OEMAKE: Additional arguments passed to make
# Example: "CFLAGS+='-O2 -g' DESTDIR='${D}'"
EXTRA_OEMAKE = "'CC=${CC}' 'CFLAGS=${CFLAGS}' 'LDFLAGS=${LDFLAGS}'"

# Dependencies for building (DEPENDS) vs runtime (RDEPENDS)
# DEPENDS: required during build time (headers, libraries)
# RDEPENDS: required on target at runtime (shared libraries)
DEPENDS = ""
RDEPENDS:${PN} = ""

# Custom task example: run before do_compile
# Uncomment if needed
# do_configure:prepend() {
#     echo "Running pre-configure setup..."
#     # Custom configuration steps here
# }

# Custom task example: run after do_install
# Uncomment if needed
# do_install:append() {
#     echo "Running post-install customization..."
#     # Install additional files
#     install -d ${D}${sysconfdir}
#     echo "# Hello World configuration" > ${D}${sysconfdir}/hello.conf
# }

# Package architecture - typically inherited automatically
# PACKAGE_ARCH = "${MACHINE_ARCH}"  # Machine-specific
# PACKAGE_ARCH = "all"               # Architecture-independent

# Files installed by this recipe
# Default is everything under ${D}
FILES:${PN} = "${bindir}/hello-world"
FILES:${PN}-dbg = "${bindir}/.debug/hello-world"
FILES:${PN}-dev = "${includedir}/hello-world.h"

# Package splitting (for advanced recipes)
# PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

# Compatibility restrictions
# COMPATIBLE_MACHINE = "(jetson-orin|qemux86-64|raspberrypi4)"
# COMPATIBLE_HOST = ""

# Example: How to add local patches
# SRC_URI += "file://0001-fix-compilation.patch \
#             file://0002-add-feature.patch \
#            "

# Example: How to add local files
# SRC_URI += "file://hello.conf"
# do_install:append() {
#     install -d ${D}${sysconfdir}
#     install -m 0644 ${WORKDIR}/hello.conf ${D}${sysconfdir}/
# }

# Build time notes:
# - Recipe is parsed during bitbake parse phase
# - Tasks execute in order: fetch -> unpack -> patch -> configure -> compile -> install -> package
# - Each task creates a stamp file in ${STAMPS_DIR}
# - Use bitbake -c cleanall hello-world to clean everything
# - Use bitbake -c compile hello-world to run specific task

# Common debugging commands:
# bitbake hello-world -c devshell      # Open interactive build environment
# bitbake hello-world -e | grep ^S=   # Show variable values
# bitbake-getvar -r hello-world S     # Get specific variable value
# bitbake hello-world -g              # Generate dependency graph
