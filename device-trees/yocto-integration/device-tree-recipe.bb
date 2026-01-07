# Device Tree Recipe for Jetson Orin Custom Hardware
#
# This recipe demonstrates how to build custom device trees and overlays
# independently from the kernel, providing flexibility for hardware variations.
#
# Usage:
#   1. Copy this recipe to your layer: recipes-bsp/device-tree/device-tree-custom_1.0.bb
#   2. Create files/ directory with your .dts and .dtso files
#   3. Add to image: IMAGE_INSTALL:append = " device-tree-custom"

SUMMARY = "Custom device trees for Jetson Orin platforms"
DESCRIPTION = "Device tree source files and overlays for custom Jetson Orin carrier boards"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

# Recipe version
PV = "1.0"
PR = "r0"

# Inherit devicetree class for device tree compilation support
inherit devicetree deploy

# Compatible machines
COMPATIBLE_MACHINE = "jetson-orin|jetson-orin-nano|jetson-orin-nx"

# Dependencies
DEPENDS = "dtc-native"

# Source files
SRC_URI = " \
    file://custom-carrier-board.dts \
    file://industrial-io-board.dts \
    file://overlays/enable-uart2.dtso \
    file://overlays/enable-spi1.dtso \
    file://overlays/i2c-sensor-bme280.dtso \
    file://overlays/gpio-led-indicator.dtso \
    file://overlays/pwm-fan-control.dtso \
    file://overlays/pcie-nvme-storage.dtso \
    file://overlays/camera-imx219.dtso \
    file://fragments/jetson-orin-pinmux.dtsi \
    file://fragments/tegra234-power.dtsi \
    file://fragments/thermal-zones.dtsi \
"

# Work directory
S = "${WORKDIR}"
B = "${WORKDIR}/build"

# DTC (Device Tree Compiler) flags
DTC_FLAGS ?= "-@"  # Enable symbol generation for overlays
DTC_INCLUDE_PATH = "${S}"

# Device trees to compile (full device trees)
DEVICE_TREES = " \
    custom-carrier-board \
    industrial-io-board \
"

# Device tree overlays to compile
DEVICE_TREE_OVERLAYS = " \
    overlays/enable-uart2 \
    overlays/enable-spi1 \
    overlays/i2c-sensor-bme280 \
    overlays/gpio-led-indicator \
    overlays/pwm-fan-control \
    overlays/pcie-nvme-storage \
    overlays/camera-imx219 \
"

# Installation directory
DEVICE_TREE_INSTALL_DIR = "/boot/dtb"
DEVICE_TREE_OVERLAY_DIR = "${DEVICE_TREE_INSTALL_DIR}/overlays"

do_configure[noexec] = "1"

do_compile() {
    # Create build directory
    mkdir -p ${B}
    mkdir -p ${B}/overlays

    # Copy include files to build directory
    cp -f ${S}/fragments/*.dtsi ${B}/

    # Compile full device trees
    for dts in ${DEVICE_TREES}; do
        bbnote "Compiling device tree: ${dts}.dts"

        if [ -f ${S}/${dts}.dts ]; then
            dtc ${DTC_FLAGS} \
                -I dts -O dtb \
                -i ${DTC_INCLUDE_PATH} \
                -i ${DTC_INCLUDE_PATH}/fragments \
                -o ${B}/${dts}.dtb \
                ${S}/${dts}.dts

            if [ $? -ne 0 ]; then
                bbfatal "Failed to compile ${dts}.dts"
            fi
        else
            bbwarn "Source file not found: ${dts}.dts"
        fi
    done

    # Compile device tree overlays
    for dtso in ${DEVICE_TREE_OVERLAYS}; do
        bbnote "Compiling overlay: ${dtso}.dtso"

        if [ -f ${S}/${dtso}.dtso ]; then
            dtc ${DTC_FLAGS} \
                -I dts -O dtb \
                -i ${DTC_INCLUDE_PATH} \
                -i ${DTC_INCLUDE_PATH}/fragments \
                -o ${B}/${dtso}.dtbo \
                ${S}/${dtso}.dtso

            if [ $? -ne 0 ]; then
                bbfatal "Failed to compile ${dtso}.dtso"
            fi
        else
            bbwarn "Source file not found: ${dtso}.dtso"
        fi
    done
}

do_install() {
    # Install base device trees
    install -d ${D}${DEVICE_TREE_INSTALL_DIR}
    for dts in ${DEVICE_TREES}; do
        if [ -f ${B}/${dts}.dtb ]; then
            install -m 0644 ${B}/${dts}.dtb ${D}${DEVICE_TREE_INSTALL_DIR}/
            bbnote "Installed: ${dts}.dtb"
        fi
    done

    # Install overlays
    install -d ${D}${DEVICE_TREE_OVERLAY_DIR}
    for dtso in ${DEVICE_TREE_OVERLAYS}; do
        dtbo_file=$(basename ${dtso}).dtbo
        if [ -f ${B}/${dtso}.dtbo ]; then
            install -m 0644 ${B}/${dtso}.dtbo ${D}${DEVICE_TREE_OVERLAY_DIR}/${dtbo_file}
            bbnote "Installed overlay: ${dtbo_file}"
        fi
    done

    # Install source files for reference (optional)
    install -d ${D}${datadir}/devicetree-sources
    install -m 0644 ${S}/*.dts ${D}${datadir}/devicetree-sources/ || true
    install -m 0644 ${S}/overlays/*.dtso ${D}${datadir}/devicetree-sources/ || true
    install -m 0644 ${S}/fragments/*.dtsi ${D}${datadir}/devicetree-sources/ || true
}

do_deploy() {
    # Deploy to DEPLOY_DIR for image building
    install -d ${DEPLOYDIR}/devicetree

    # Deploy base device trees
    for dts in ${DEVICE_TREES}; do
        if [ -f ${B}/${dts}.dtb ]; then
            install -m 0644 ${B}/${dts}.dtb ${DEPLOYDIR}/devicetree/
        fi
    done

    # Deploy overlays
    install -d ${DEPLOYDIR}/devicetree/overlays
    for dtso in ${DEVICE_TREE_OVERLAYS}; do
        dtbo_file=$(basename ${dtso}).dtbo
        if [ -f ${B}/${dtso}.dtbo ]; then
            install -m 0644 ${B}/${dtso}.dtbo ${DEPLOYDIR}/devicetree/overlays/${dtbo_file}
        fi
    done
}

addtask deploy before do_build after do_compile

# Package files
FILES:${PN} = " \
    ${DEVICE_TREE_INSTALL_DIR}/*.dtb \
    ${DEVICE_TREE_OVERLAY_DIR}/*.dtbo \
    ${datadir}/devicetree-sources/* \
"

# Make DTB files available to other recipes
PROVIDES = "virtual/dtb"

# Allow empty package if no device trees compiled
ALLOW_EMPTY:${PN} = "1"

# Runtime dependencies (if any special drivers needed)
RDEPENDS:${PN} = ""

# Development package for sources
PACKAGES =+ "${PN}-src"
FILES:${PN}-src = "${datadir}/devicetree-sources/*"

# Package architecture
PACKAGE_ARCH = "${MACHINE_ARCH}"

# Example of machine-specific configuration
# Uncomment and modify for your needs
#
# python do_compile:prepend() {
#     machine = d.getVar('MACHINE')
#     if machine == "jetson-orin-nano":
#         d.setVar('DEVICE_TREES', 'custom-carrier-board')
#     elif machine == "jetson-orin-nx":
#         d.setVar('DEVICE_TREES', 'industrial-io-board')
# }

# Example: Validate device trees after compilation
# do_compile:append() {
#     for dtb in ${B}/*.dtb; do
#         if [ -f "$dtb" ]; then
#             dtc -I dtb -O dts "$dtb" > /dev/null
#             if [ $? -ne 0 ]; then
#                 bbwarn "Device tree validation failed: $dtb"
#             fi
#         fi
#     done
# }

# Example: Create README for installation
# do_install:append() {
#     cat > ${D}${DEVICE_TREE_INSTALL_DIR}/README.txt << 'EOF'
# Device Tree Installation
# ========================
#
# To use these device trees:
#
# 1. For full device tree replacement:
#    - Copy desired .dtb to /boot/
#    - Update bootloader configuration (extlinux.conf or U-Boot)
#
# 2. For overlays:
#    - Overlays are in /boot/dtb/overlays/
#    - Add to extlinux.conf:
#      FDTOVERLAYS /boot/dtb/overlays/enable-uart2.dtbo
#
# 3. Verify loading:
#    - dmesg | grep -i "device tree"
#    - cat /proc/device-tree/compatible
# EOF
# }
