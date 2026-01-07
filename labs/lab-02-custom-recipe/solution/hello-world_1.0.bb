SUMMARY = "Simple Hello World application for Yocto learning"
DESCRIPTION = "A basic C program demonstrating custom recipe creation, \
compilation, and installation in the Yocto Project build system. \
This recipe shows proper use of do_compile and do_install tasks."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Source files located in files/ subdirectory
SRC_URI = "file://hello-world.c \
           file://Makefile \
          "

# Source directory is the WORKDIR (where BitBake extracts files)
S = "${WORKDIR}"

# Installation directories
bindir = "/usr/bin"

# Compile the application using the provided Makefile
do_compile() {
    # oe_runmake is a BitBake function that invokes make with
    # proper cross-compilation environment variables set
    oe_runmake
}

# Install the compiled binary to the target filesystem staging area
do_install() {
    # Create destination directory
    # ${D} is the staging directory root
    # ${bindir} expands to /usr/bin
    install -d ${D}${bindir}

    # Install the binary with executable permissions
    # -m 0755 sets read/write/execute for owner, read/execute for others
    install -m 0755 hello-world ${D}${bindir}/hello-world
}

# Explicitly specify files included in this package
# This helps BitBake understand what files belong where
FILES:${PN} = "${bindir}/hello-world"

# Runtime dependencies (packages needed on target)
# Empty for this simple example
RDEPENDS:${PN} = ""

# Build-time dependencies (other recipes needed during build)
# Empty for this simple example
DEPENDS = ""

# Package architecture
# Use MACHINE_ARCH if binary is machine-specific
# Use "all" if package is architecture-independent (scripts, configs)
PACKAGE_ARCH = "${MACHINE_ARCH}"
