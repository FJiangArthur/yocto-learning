# Yocto Quick Reference Guide

## Essential BitBake Commands

### Building

```bash
# Build a specific recipe
bitbake <recipe-name>

# Build a complete image
bitbake core-image-minimal
bitbake custom-image

# Build with verbose output
bitbake -v <recipe-name>

# Show what will be built (dry run)
bitbake -n <recipe-name>

# Continue building despite errors
bitbake -k <recipe-name>

# Force rebuild (ignore shared state cache)
bitbake -f <recipe-name>

# Build specific task only
bitbake -c <task> <recipe-name>
# Example: bitbake -c compile linux-tegra
```

### Cleaning

```bash
# Clean a recipe (remove temp files, keep downloads)
bitbake -c clean <recipe-name>

# Clean everything including downloads
bitbake -c cleanall <recipe-name>

# Clean shared state cache for a recipe
bitbake -c cleansstate <recipe-name>

# Remove all build artifacts (nuclear option)
rm -rf tmp/
```

### Information & Debugging

```bash
# Show recipe information
bitbake -e <recipe-name> | less

# Show specific variable value
bitbake -e <recipe-name> | grep ^VARIABLE_NAME=

# List all available recipes
bitbake -s

# Show recipe dependencies
bitbake -g <recipe-name>
# Creates: task-depends.dot, pn-depends.dot

# Show why a package is being built
bitbake -g <recipe-name> && cat pn-buildlist

# Enter development shell
bitbake -c devshell <recipe-name>

# Enter Python debug shell
bitbake -c devpyshell <recipe-name>

# Show all tasks for a recipe
bitbake -c listtasks <recipe-name>
```

### Package Management

```bash
# List files in a package
bitbake -c package_write_rpm <recipe-name>
oe-pkgdata-util list-pkg-files <package-name>

# Find which recipe provides a file
oe-pkgdata-util find-path /path/to/file

# Search for packages containing pattern
oe-pkgdata-util lookup-recipe <pattern>
```

---

## Critical BitBake Variables

### Source & Fetching

```bash
# Source URI (where to download from)
SRC_URI = "git://github.com/user/repo.git;protocol=https;branch=main"
SRC_URI = "https://example.com/file.tar.gz"
SRC_URI = "file://local-file.patch"

# Source revision (for git)
SRCREV = "1a2b3c4d5e6f7890abcdef"
SRCREV = "${AUTOREV}"  # Always use latest (not recommended)

# Checksum verification
SRC_URI[md5sum] = "abc123..."
SRC_URI[sha256sum] = "def456..."

# Local files path
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
```

### Licensing

```bash
# License identifier
LICENSE = "MIT"
LICENSE = "GPLv2 & LGPLv2.1"

# License file checksum (to track changes)
LIC_FILES_CHKSUM = "file://LICENSE;md5=abc123..."
```

### Dependencies

```bash
# Build-time dependencies (native tools, headers)
DEPENDS = "cmake-native python3 openssl"

# Runtime dependencies (shared libraries, utilities)
RDEPENDS:${PN} = "bash python3-core libssl"

# Runtime recommendations (optional)
RRECOMMENDS:${PN} = "ca-certificates"

# Package conflicts
RCONFLICTS:${PN} = "old-package-name"

# Package provides (virtual packages)
PROVIDES = "virtual/kernel"
```

### Versioning & Naming

```bash
# Package name (auto-set from recipe filename)
PN = "hello-world"

# Package version
PV = "1.0"

# Package revision
PR = "r0"

# Full package name
P = "${PN}-${PV}"  # hello-world-1.0

# Package name with epoch
PKGE = "1"
```

### File Installation

```bash
# Files to include in package
FILES:${PN} = "${bindir}/myapp ${sysconfdir}/myapp/*"
FILES:${PN}-dev = "${includedir}/* ${libdir}/*.so"
FILES:${PN}-dbg = "${bindir}/.debug ${libdir}/.debug"

# Common directory variables
bindir = "/usr/bin"
sbindir = "/usr/sbin"
libdir = "/usr/lib"
includedir = "/usr/include"
sysconfdir = "/etc"
datadir = "/usr/share"
localstatedir = "/var"
```

### Build Configuration

```bash
# Inherit classes
inherit cmake
inherit autotools pkgconfig
inherit systemd

# Compilation flags
EXTRA_OECONF = "--enable-feature --disable-other"
EXTRA_OECMAKE = "-DENABLE_FEATURE=ON"
EXTRA_OEMAKE = "CFLAGS='-O2 -g'"

# Parallel make
PARALLEL_MAKE = "-j 8"

# Installation directory
D = "${WORKDIR}/image"  # Destination for do_install

# Source directory
S = "${WORKDIR}/git"
S = "${WORKDIR}/${PN}-${PV}"
```

### Image Configuration

```bash
# Image features
IMAGE_FEATURES = "ssh-server-openssh package-management"

# Packages to install
IMAGE_INSTALL:append = " python3 vim curl"

# Root filesystem size
IMAGE_ROOTFS_SIZE = "8192"

# Extra space (in KB)
IMAGE_ROOTFS_EXTRA_SPACE = "1048576"

# Filesystem type
IMAGE_FSTYPES = "ext4 tar.gz wic"
```

### Machine & Distro

```bash
# Target machine
MACHINE = "qemux86-64"
MACHINE = "jetson-agx-orin-devkit"

# Distribution
DISTRO = "poky"

# Target architecture
TARGET_ARCH = "aarch64"

# Build directory
TOPDIR = "/path/to/build"
```

### Layer Configuration

```bash
# Layer priority (higher = more important)
BBFILE_PRIORITY_meta-custom = "7"

# Layer dependencies
LAYERDEPENDS_meta-custom = "core meta-oe"

# Layer compatibility
LAYERSERIES_COMPAT_meta-custom = "kirkstone scarthgap"
```

---

## Standard Directory Structure

```
poky/                          # Yocto reference distribution
├── build/                     # Build directory (created by oe-init-build-env)
│   ├── conf/
│   │   ├── local.conf        # Local build configuration
│   │   ├── bblayers.conf     # Layer configuration
│   │   └── templateconf.cfg  # Template configuration
│   ├── tmp/                   # Build artifacts
│   │   ├── deploy/
│   │   │   ├── images/       # Final images
│   │   │   ├── rpm/          # RPM packages
│   │   │   ├── ipk/          # IPK packages
│   │   │   └── licenses/     # License manifests
│   │   ├── work/             # Per-recipe work directories
│   │   ├── sysroots/         # Cross-compilation sysroots
│   │   └── sysroots-components/
│   ├── cache/                 # BitBake cache
│   └── sstate-cache/         # Shared state cache
│
├── meta/                      # Core layer (Poky)
├── meta-poky/                 # Poky-specific layer
├── meta-yocto-bsp/           # BSP layer
└── meta-*/                    # Additional layers

meta-layer/                    # Custom layer structure
├── conf/
│   └── layer.conf            # Layer configuration
├── recipes-*/                 # Recipe directories
│   ├── <category>/
│   │   └── <recipe>/
│   │       ├── <recipe>_<version>.bb
│   │       └── files/
│   │           ├── 0001-patch.patch
│   │           └── config-file
└── classes/                   # Custom classes
```

### Work Directory Structure

```
tmp/work/<arch>/<recipe>/<version>/
├── temp/                      # Task logs and scripts
│   ├── log.do_fetch          # Fetch log
│   ├── log.do_compile        # Compile log
│   └── run.do_install        # Install script
├── <source>/                  # Unpacked source
├── build/                     # Build artifacts
├── image/                     # Installed files (${D})
├── package/                   # Split packages
├── packages-split/
│   ├── <package-name>/       # Individual package contents
│   └── <package-name>-dev/
└── deploy-*/                  # Deployed packages
```

---

## Recipe Template (Copy-Paste Ready)

### Simple Application Recipe

```bitbake
SUMMARY = "Short description of the package"
DESCRIPTION = "Longer description explaining what this package does"
HOMEPAGE = "https://example.com"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=<checksum>"

SRC_URI = "git://github.com/user/repo.git;protocol=https;branch=main \
           file://0001-custom-patch.patch \
           file://config.ini \
          "
SRCREV = "1a2b3c4d5e6f7890abcdef"

S = "${WORKDIR}/git"

# Dependencies
DEPENDS = "cmake-native"
RDEPENDS:${PN} = "bash"

# Inherit build system class
inherit cmake

# Extra configuration
EXTRA_OECMAKE = "-DENABLE_FEATURE=ON -DCMAKE_BUILD_TYPE=Release"

# Install additional files
do_install:append() {
    install -d ${D}${sysconfdir}/${PN}
    install -m 0644 ${WORKDIR}/config.ini ${D}${sysconfdir}/${PN}/
}

# Package files
FILES:${PN} += "${sysconfdir}/${PN}/*"
```

### Kernel Module Recipe

```bitbake
SUMMARY = "Custom kernel module"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=<checksum>"

inherit module

SRC_URI = "file://Kbuild \
           file://hello.c \
          "

S = "${WORKDIR}"

# Kernel version dependency
RPROVIDES:${PN} += "kernel-module-hello"
```

### Python Application Recipe

```bitbake
SUMMARY = "Python application"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=<checksum>"

SRC_URI = "https://pypi.python.org/packages/source/p/${PN}/${PN}-${PV}.tar.gz"
SRC_URI[sha256sum] = "<checksum>"

inherit setuptools3

RDEPENDS:${PN} = "python3-core python3-requests"
```

### systemd Service Recipe

```bitbake
SUMMARY = "Application with systemd service"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=<checksum>"

SRC_URI = "file://myapp \
           file://myapp.service \
          "

inherit systemd

SYSTEMD_SERVICE:${PN} = "myapp.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    # Install application
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/myapp ${D}${bindir}/

    # Install systemd service
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/myapp.service ${D}${systemd_system_unitdir}/
}

FILES:${PN} += "${systemd_system_unitdir}/myapp.service"
RDEPENDS:${PN} += "systemd"
```

### Custom Image Recipe

```bitbake
SUMMARY = "Custom Linux image"
LICENSE = "MIT"

inherit core-image

# Base image features
IMAGE_FEATURES += "ssh-server-openssh package-management"

# Additional packages
IMAGE_INSTALL:append = " \
    packagegroup-core-boot \
    python3 \
    vim \
    htop \
    custom-app \
"

# Image size (in KB)
IMAGE_ROOTFS_EXTRA_SPACE = "2048000"

# Filesystem types
IMAGE_FSTYPES = "ext4 tar.gz"

# Post-install commands
ROOTFS_POSTPROCESS_COMMAND += "my_custom_function; "

my_custom_function() {
    # Custom modifications to rootfs
    echo "Custom configuration" > ${IMAGE_ROOTFS}/etc/custom.conf
}
```

---

## Common Debugging Commands

### Investigate Build Failures

```bash
# 1. Check the error log
bitbake <recipe-name> 2>&1 | tee build.log

# 2. Examine detailed task logs
cat tmp/work/<arch>/<recipe>/<version>/temp/log.do_<task>

# 3. Enter development shell at failure point
bitbake -c devshell <recipe-name>

# 4. Manually run failed task
cd tmp/work/<arch>/<recipe>/<version>
./temp/run.do_<task>

# 5. Check variable expansion
bitbake -e <recipe-name> | grep ^VARIABLE_NAME=
```

### Dependency Issues

```bash
# Show why a package is needed
bitbake -g <image-recipe>
cat pn-buildlist | grep <package-name>

# Check runtime dependencies
oe-pkgdata-util read-value RDEPENDS <package-name>

# Find circular dependencies
bitbake -g <recipe-name>
# Look for cycles in task-depends.dot
```

### Package Investigation

```bash
# List all packages from a recipe
oe-pkgdata-util list-pkgs -p <recipe-name>

# Show package contents
oe-pkgdata-util list-pkg-files <package-name>

# Find which package provides a file
oe-pkgdata-util find-path /usr/bin/program

# Show package dependencies
opkg-query -i <package-name>
```

### Layer Issues

```bash
# List all layers
bitbake-layers show-layers

# Add a layer
bitbake-layers add-layer /path/to/meta-layer

# Remove a layer
bitbake-layers remove-layer meta-layer

# Show recipes from a layer
bitbake-layers show-recipes -i meta-layer

# Show append files
bitbake-layers show-appends
```

### Shared State Cache

```bash
# Show shared state cache usage
sstate-cache-management.sh -L

# Remove unused sstate
sstate-cache-management.sh -y

# Show what would be removed (dry run)
sstate-cache-management.sh -d -y
```

---

## Environment Setup

### Initialize Build Environment

```bash
# Standard Poky setup
cd poky
source oe-init-build-env [build-directory]

# With template configuration
source oe-init-build-env -t meta-layer/conf

# Multiple build directories
source oe-init-build-env build-x86
source oe-init-build-env build-arm
```

### Essential local.conf Settings

```bash
# Machine selection
MACHINE = "jetson-agx-orin-devkit"

# Parallel build settings
BB_NUMBER_THREADS = "8"
PARALLEL_MAKE = "-j 8"

# Disk space monitoring
BB_DISKMON_DIRS = "STOPTASKS,${TMPDIR},1G,100K STOPTASKS,${DL_DIR},1G,100K"

# Download directory (reusable across builds)
DL_DIR = "/path/to/downloads"

# Shared state directory (reusable across builds)
SSTATE_DIR = "/path/to/sstate-cache"

# Package feed
PACKAGE_CLASSES = "package_rpm"

# Remove old images
RM_OLD_IMAGE = "1"

# SDK configuration
SDKMACHINE = "x86_64"
```

---

## Performance Tips

### Speed Up Builds

```bash
# Use more CPU cores (set to number of cores)
BB_NUMBER_THREADS = "16"
PARALLEL_MAKE = "-j 16"

# Use shared download and sstate directories
DL_DIR = "/shared/downloads"
SSTATE_DIR = "/shared/sstate-cache"

# Use local mirrors
PREMIRRORS:prepend = "\
git://.*/.* http://local-mirror.com/git/MIRRORNAME \n \
https://.*/.* http://local-mirror.com/https/MIRRORNAME \n \
"

# Enable hash equivalence (build server)
BB_HASHSERVE = "auto"
BB_SIGNATURE_HANDLER = "OEEquivHash"

# Use tmpfs for temporary files (if enough RAM)
# In fstab: tmpfs /path/to/build/tmp tmpfs defaults,size=32G 0 0
```

### Reduce Disk Usage

```bash
# Clean up after builds
INHERIT += "rm_work"

# Keep work files for specific recipes
RM_WORK_EXCLUDE += "recipe-name"

# Limit sstate cache size
# Use sstate-cache-management.sh periodically
```

---

## Recipe Writing Best Practices

### Variable Assignment

```bash
# Simple assignment (parsed at recipe load)
VAR = "value"

# Immediate assignment (expanded immediately)
VAR := "value with ${OTHER_VAR}"

# Weak assignment (only if not already set)
VAR ?= "default value"

# Append with space
VAR:append = " additional"

# Prepend with space
VAR:prepend = "prefix "

# Remove value
VAR:remove = "unwanted-value"
```

### Override Syntax (New Style)

```bash
# Machine-specific
VARIABLE:machine-name = "value"

# Architecture-specific
VARIABLE:aarch64 = "value"

# Class-specific
VARIABLE:class-target = "value"
VARIABLE:class-native = "value"

# Package-specific
FILES:${PN} = "value"
RDEPENDS:${PN}-dev = "value"
```

### Task Dependencies

```bash
# Task must run before another
do_compile[depends] = "recipe:do_task"

# Runtime dependency
RDEPENDS:${PN} = "package"

# Build dependency
DEPENDS = "recipe"

# Add task before/after
addtask mytask before do_build after do_compile

# Task function
do_mytask() {
    # Task implementation
}
```

---

## Common Use Cases

### Building for QEMU

```bash
# Set machine
MACHINE = "qemux86-64"

# Build minimal image
bitbake core-image-minimal

# Run in QEMU
runqemu qemux86-64

# Run with graphics
runqemu qemux86-64 nographic

# Run with custom kernel
runqemu qemux86-64 slirp
```

### Creating SDK

```bash
# Build SDK for host
bitbake <image> -c populate_sdk

# Build extensible SDK
bitbake <image> -c populate_sdk_ext

# SDK location
ls tmp/deploy/sdk/
```

### Working with Patches

```bash
# Create patch from git
git format-patch -1 <commit-hash>

# Add patch to recipe
SRC_URI += "file://0001-my-patch.patch"

# Apply patches in specific order
SRC_URI += "file://0001-first.patch \
            file://0002-second.patch \
           "

# Generate patch from devshell
bitbake -c devshell <recipe>
# Make changes, then:
git diff > my-changes.patch
```

---

## Troubleshooting Quick Checks

### Build Won't Start

```bash
# Check BitBake version
bitbake --version

# Verify environment
echo $BUILDDIR

# Check layer configuration
bitbake-layers show-layers
```

### Recipe Not Found

```bash
# Search for recipe
bitbake -s | grep recipe-name

# Show recipe location
bitbake -e recipe-name | grep ^FILE=

# Check layer priority
bitbake-layers show-layers
```

### Fetch Failures

```bash
# Check network
ping -c 3 github.com

# Verify SRC_URI
bitbake -e recipe-name | grep ^SRC_URI=

# Try manual download
wget <url-from-SRC_URI>

# Use local mirror
DL_DIR = "/local/downloads"
```

### Compilation Errors

```bash
# Check build logs
cat tmp/work/.../temp/log.do_compile

# Enter devshell
bitbake -c devshell recipe-name

# Check dependencies
bitbake -e recipe-name | grep ^DEPENDS=

# Verify toolchain
bitbake -e recipe-name | grep ^CC=
```

---

## Quick Command Cheatsheet

```bash
# Most common commands (copy this!)
bitbake <recipe>                    # Build recipe
bitbake -c clean <recipe>           # Clean recipe
bitbake -c devshell <recipe>        # Debug recipe
bitbake -e <recipe> | grep VAR      # Check variable
bitbake -g <recipe>                 # Show dependencies
bitbake-layers show-layers          # List layers
bitbake-layers add-layer <path>     # Add layer
oe-pkgdata-util list-pkgs           # List all packages
runqemu <machine>                   # Run QEMU
```

---

## Additional Resources

### Log Locations

```
tmp/work/<arch>/<recipe>/<version>/temp/
├── log.do_fetch           # Download log
├── log.do_unpack          # Extraction log
├── log.do_patch           # Patch application log
├── log.do_configure       # Configuration log
├── log.do_compile         # Compilation log
├── log.do_install         # Installation log
└── log.do_package         # Packaging log
```

### Environment Variables

```bash
BBPATH              # BitBake search path
BUILDDIR            # Current build directory
TMPDIR              # Temporary build files
WORKDIR             # Recipe work directory
S                   # Source directory
D                   # Destination (install) directory
PN                  # Package name
PV                  # Package version
```

### Useful Classes to Inherit

```bash
inherit autotools          # Autoconf/automake projects
inherit cmake              # CMake projects
inherit meson              # Meson projects
inherit setuptools3        # Python packages
inherit systemd            # systemd services
inherit kernel             # Linux kernel
inherit module             # Kernel modules
inherit native             # Native tools
inherit cross              # Cross-compilation tools
```

---

**Quick Reference Version:** 1.0
**Last Updated:** December 2024

For comprehensive tutorials, see `/labs/` directory.
For example recipes, see `/recipes/` directory.
For interview questions, see `/interview-questions/` directory.
