# Kernel Device Tree BBAppend for Jetson Orin
#
# This bbappend demonstrates how to add custom device trees and overlays
# to the kernel build process. This is the most common approach for
# integrating device trees in Yocto.
#
# Usage:
#   1. Copy to: recipes-kernel/linux/linux-tegra_%.bbappend
#   2. Create files/linux-tegra/ directory with your device tree sources
#   3. Rebuild kernel: bitbake -c cleansstate linux-tegra && bitbake linux-tegra
#
# File: recipes-kernel/linux/linux-tegra_%.bbappend

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SUMMARY = "Custom device trees for Jetson Orin platforms"

#
# Method 1: Add device tree source files to kernel build
# =======================================================
#

SRC_URI:append = " \
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

#
# Enable symbol generation for overlays
# This allows overlays to reference nodes from base device tree
#
DTC_FLAGS += "-@"

#
# Copy device tree sources to kernel tree before compilation
#
do_configure:prepend() {
    # Define kernel device tree directories
    DT_DIR="${S}/arch/arm64/boot/dts/nvidia"
    DT_OVERLAY_DIR="${DT_DIR}/overlays"
    DT_FRAGMENTS_DIR="${DT_DIR}/fragments"

    # Create directories if they don't exist
    mkdir -p ${DT_OVERLAY_DIR}
    mkdir -p ${DT_FRAGMENTS_DIR}

    # Copy base device trees
    if [ -f ${WORKDIR}/custom-carrier-board.dts ]; then
        cp ${WORKDIR}/custom-carrier-board.dts ${DT_DIR}/
        bbnote "Copied custom-carrier-board.dts to kernel source"
    fi

    if [ -f ${WORKDIR}/industrial-io-board.dts ]; then
        cp ${WORKDIR}/industrial-io-board.dts ${DT_DIR}/
        bbnote "Copied industrial-io-board.dts to kernel source"
    fi

    # Copy overlays
    for overlay in enable-uart2 enable-spi1 i2c-sensor-bme280 \
                    gpio-led-indicator pwm-fan-control pcie-nvme-storage \
                    camera-imx219; do
        if [ -f ${WORKDIR}/overlays/${overlay}.dtso ]; then
            cp ${WORKDIR}/overlays/${overlay}.dtso ${DT_OVERLAY_DIR}/
            bbnote "Copied ${overlay}.dtso to kernel overlays"
        fi
    done

    # Copy fragment includes
    for fragment in jetson-orin-pinmux tegra234-power thermal-zones; do
        if [ -f ${WORKDIR}/fragments/${fragment}.dtsi ]; then
            cp ${WORKDIR}/fragments/${fragment}.dtsi ${DT_FRAGMENTS_DIR}/
            bbnote "Copied ${fragment}.dtsi to kernel fragments"
        fi
    done
}

#
# Method 2: Specify device trees to build
# ========================================
#
# Add custom device trees to KERNEL_DEVICETREE variable
# This tells the kernel build system which device trees to compile
#

# For full device trees (complete hardware description)
KERNEL_DEVICETREE:append = " \
    nvidia/custom-carrier-board.dtb \
    nvidia/industrial-io-board.dtb \
"

# For device tree overlays
KERNEL_DEVICETREE:append = " \
    nvidia/overlays/enable-uart2.dtbo \
    nvidia/overlays/enable-spi1.dtbo \
    nvidia/overlays/i2c-sensor-bme280.dtbo \
    nvidia/overlays/gpio-led-indicator.dtbo \
    nvidia/overlays/pwm-fan-control.dtbo \
    nvidia/overlays/pcie-nvme-storage.dtbo \
    nvidia/overlays/camera-imx219.dtbo \
"

#
# Method 3: Machine-specific device trees
# ========================================
#
# Use different device trees based on MACHINE variable
#

# Example for machine-specific configuration
# KERNEL_DEVICETREE:jetson-orin-nano = "nvidia/custom-carrier-board.dtb"
# KERNEL_DEVICETREE:jetson-orin-nx = "nvidia/industrial-io-board.dtb"

#
# Alternative: Conditional compilation based on DISTRO_FEATURES
#
# python() {
#     features = d.getVar('DISTRO_FEATURES').split()
#     dt_list = d.getVar('KERNEL_DEVICETREE') or ""
#
#     if 'camera' in features:
#         dt_list += " nvidia/overlays/camera-imx219.dtbo"
#
#     if 'can' in features:
#         dt_list += " nvidia/industrial-io-board.dtb"
#
#     d.setVar('KERNEL_DEVICETREE', dt_list)
# }

#
# Method 4: Modify existing device tree via patches
# ==================================================
#
# For minor changes to existing device trees, use patches
#

# SRC_URI:append = " \
#     file://0001-enable-uart2-on-40pin-header.patch \
#     file://0002-add-camera-sensor-support.patch \
# "

#
# Installation customization
# ===========================
#

# Install device trees to specific location
do_install:append() {
    # Additional installation steps if needed
    # Device trees are automatically installed to /boot by kernel recipe

    # Example: Create symlinks for convenience
    if [ -d ${D}/boot ]; then
        # Create default.dtb symlink to your custom board
        if [ -f ${D}/boot/custom-carrier-board.dtb ]; then
            ln -sf custom-carrier-board.dtb ${D}/boot/default.dtb
        fi
    fi

    # Example: Install overlay configuration file
    install -d ${D}${sysconfdir}/overlays
    cat > ${D}${sysconfdir}/overlays/default-overlays.conf << 'EOF'
# Default overlays to load
# Format: one overlay per line (filename without .dtbo extension)
enable-uart2
enable-spi1
i2c-sensor-bme280
EOF
}

# Add configuration file to package
FILES:${PN}:append = " ${sysconfdir}/overlays/*"

#
# Deployment customization
# =========================
#

do_deploy:append() {
    # Device trees are automatically deployed to DEPLOY_DIR_IMAGE
    # Add any custom deployment logic here

    # Example: Create a manifest file
    cd ${DEPLOYDIR}
    if ls *.dtb >/dev/null 2>&1; then
        echo "Device Trees:" > devicetree-manifest.txt
        ls -1 *.dtb >> devicetree-manifest.txt
    fi

    if [ -d overlays ] && ls overlays/*.dtbo >/dev/null 2>&1; then
        echo "" >> devicetree-manifest.txt
        echo "Overlays:" >> devicetree-manifest.txt
        ls -1 overlays/*.dtbo >> devicetree-manifest.txt
    fi
}

#
# Validation and testing
# =======================
#

# Example: Validate device tree after compilation
# do_compile:append() {
#     DT_DIR="${B}/arch/arm64/boot/dts/nvidia"
#
#     for dtb in ${DT_DIR}/*.dtb; do
#         if [ -f "$dtb" ]; then
#             bbnote "Validating: $(basename $dtb)"
#             dtc -I dtb -O dts "$dtb" > /dev/null 2>&1
#             if [ $? -ne 0 ]; then
#                 bbwarn "Validation failed for: $(basename $dtb)"
#             fi
#         fi
#     done
#
#     for dtbo in ${DT_DIR}/overlays/*.dtbo; do
#         if [ -f "$dtbo" ]; then
#             bbnote "Validating overlay: $(basename $dtbo)"
#             dtc -@ -I dtb -O dts "$dtbo" > /dev/null 2>&1
#             if [ $? -ne 0 ]; then
#                 bbwarn "Validation failed for: $(basename $dtbo)"
#             fi
#         fi
#     done
# }

#
# Configuration notes and documentation
# ======================================
#

# IMPORTANT NOTES:
# ================
#
# 1. Device Tree Compilation Order:
#    - Base .dtsi files are included first
#    - Full .dts files are compiled to .dtb
#    - Overlays .dtso are compiled to .dtbo with -@ flag
#
# 2. Include Paths:
#    - Kernel includes: arch/arm64/boot/dts/
#    - Additional paths can be added with: DTC_INCLUDE += "path"
#
# 3. Symbol Generation:
#    - Required for overlays to reference base device tree nodes
#    - Enabled with DTC_FLAGS += "-@"
#    - Base device tree must also be compiled with -@
#
# 4. Bootloader Configuration:
#    - Device trees must be referenced in bootloader (U-Boot/extlinux)
#    - Overlays are applied at boot time
#    - See bootloader recipe for configuration
#
# 5. Machine Configuration:
#    - Default device tree is set in machine conf file
#    - KERNEL_DEVICETREE in machine conf is appended by this bbappend
#
# 6. Testing:
#    - After build, check DEPLOY_DIR_IMAGE for .dtb and .dtbo files
#    - On target, verify with: cat /proc/device-tree/compatible
#    - Check kernel log: dmesg | grep -i "device tree"
#
# 7. Debugging:
#    - Decompile DTB to verify: dtc -I dtb -O dts file.dtb
#    - Check symbols: dtc -I dtb -O dts -@ file.dtb | grep __symbols__
#    - Overlay application: cat /sys/firmware/devicetree/base/<path>/status

#
# Integration with other recipes
# ===============================
#

# If using custom bootloader configuration
# DEPENDS += "custom-bootloader-config"

# If device tree requires specific kernel config
# KERNEL_FEATURES:append = " cfg/custom-hardware.cfg"

#
# Advanced: Runtime device tree modification
# ===========================================
#

# Example package for runtime overlay management
# PACKAGES =+ "${PN}-overlay-manager"
# FILES:${PN}-overlay-manager = "${bindir}/overlay-manager"
#
# do_install:append() {
#     install -d ${D}${bindir}
#     cat > ${D}${bindir}/overlay-manager << 'EOF'
# #!/bin/sh
# # Simple overlay manager script
# OVERLAY_DIR="/boot/dtb/overlays"
# CONFIGFS_DIR="/sys/kernel/config/device-tree/overlays"
#
# case "$1" in
#     load)
#         mkdir -p ${CONFIGFS_DIR}/$2
#         cat ${OVERLAY_DIR}/$2.dtbo > ${CONFIGFS_DIR}/$2/dtbo
#         ;;
#     unload)
#         rmdir ${CONFIGFS_DIR}/$2
#         ;;
#     *)
#         echo "Usage: $0 {load|unload} overlay-name"
#         exit 1
#         ;;
# esac
# EOF
#     chmod +x ${D}${bindir}/overlay-manager
# }
