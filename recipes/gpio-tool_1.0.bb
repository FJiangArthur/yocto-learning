# gpio-tool_1.0.bb - GPIO utility for Jetson Orin
#
# This recipe demonstrates:
# - Building hardware-specific utilities
# - libgpiod integration for modern GPIO control
# - Machine-specific recipes (COMPATIBLE_MACHINE)
# - udev rules for device permissions
# - Python bindings for C libraries
#
# Learning objectives:
# 1. Understand libgpiod vs deprecated sysfs GPIO
# 2. Learn machine-specific recipe constraints
# 3. Practice hardware abstraction layer creation
# 4. Handle GPIO permissions and security

SUMMARY = "GPIO control utility for NVIDIA Jetson platforms"
DESCRIPTION = "Command-line and library interface for GPIO control on Jetson \
devices using modern libgpiod interface. Provides Python bindings and supports \
Jetson Orin GPIO banks with proper Tegra naming convention."

HOMEPAGE = "https://github.com/example/jetson-gpio-tool"
SECTION = "utils"
BUGTRACKER = "https://github.com/example/jetson-gpio-tool/issues"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = " \
    git://github.com/example/jetson-gpio-tool.git;protocol=https;branch=main \
    file://60-gpio-permissions.rules \
    file://gpio-tool.conf \
"

SRCREV = "fedcba0987654321fedcba0987654321fedcba09"

S = "${WORKDIR}/git"

# Build dependencies
# libgpiod: Modern GPIO control library (replaces sysfs)
# python3-native: For Python bindings build
DEPENDS = " \
    libgpiod \
    python3-native \
"

# Runtime dependencies
RDEPENDS:${PN} = " \
    libgpiod \
    libgpiod-tools \
"

# Python bindings package
RDEPENDS:${PN}-python = " \
    ${PN} \
    python3-core \
    libgpiod-python \
"

# Compatible only with Jetson platforms
# This prevents accidental installation on incompatible hardware
COMPATIBLE_MACHINE = "(tegra234|jetson-orin-agx|jetson-orin-nano)"

# Machine-specific builds (different per platform)
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit autotools pkgconfig

# Add Python support
inherit python3native

# Extra configure options
EXTRA_OECONF = " \
    --enable-python-bindings \
    --with-gpio-chip=/dev/gpiochip0 \
    --enable-tegra-naming \
"

# Jetson Orin specific GPIO configuration
EXTRA_OEMAKE += " \
    TEGRA_SOC=tegra234 \
    GPIO_BANKS=28 \
"

do_compile:prepend() {
    # Generate Jetson Orin GPIO mapping table
    cat > ${S}/gpio_map_tegra234.h << 'EOF'
/* Auto-generated GPIO mapping for Tegra234 (Orin) */
#ifndef GPIO_MAP_TEGRA234_H
#define GPIO_MAP_TEGRA234_H

struct tegra_gpio_port {
    const char *name;
    unsigned int base;
};

/* Tegra234 GPIO port base addresses */
static const struct tegra_gpio_port tegra234_gpio_ports[] = {
    {"PA", 316}, {"PB", 324}, {"PC", 332}, {"PD", 340},
    {"PE", 348}, {"PF", 356}, {"PG", 364}, {"PH", 372},
    {"PI", 380}, {"PJ", 388}, {"PK", 396}, {"PL", 404},
    {"PM", 412}, {"PN", 420}, {"PO", 428}, {"PP", 436},
    {"PQ", 443}, {"PR", 451}, {"PS", 459}, {"PT", 467},
    {"PU", 475}, {"PV", 483}, {"PW", 491}, {"PX", 499},
    {"PY", 507}, {"PZ", 515}, {"PAA", 523}, {"PAB", 531},
    {"PAC", 539}, {"PAD", 547}, {"PAE", 555}, {"PAF", 563},
    {"PAG", 571},
};

#define TEGRA234_GPIO_PORTS (sizeof(tegra234_gpio_ports) / sizeof(tegra234_gpio_ports[0]))

#endif /* GPIO_MAP_TEGRA234_H */
EOF
}

do_install:append() {
    # Install udev rules for GPIO permissions
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${WORKDIR}/60-gpio-permissions.rules \
        ${D}${sysconfdir}/udev/rules.d/

    # Install configuration file
    install -d ${D}${sysconfdir}/gpio-tool
    install -m 0644 ${WORKDIR}/gpio-tool.conf \
        ${D}${sysconfdir}/gpio-tool/

    # Install Python bindings
    if [ -d ${S}/python ]; then
        install -d ${D}${PYTHON_SITEPACKAGES_DIR}
        cp -r ${S}/python/jetson_gpio ${D}${PYTHON_SITEPACKAGES_DIR}/
    fi

    # Install example scripts
    install -d ${D}${datadir}/gpio-tool/examples
    if [ -d ${S}/examples ]; then
        install -m 0755 ${S}/examples/*.sh ${D}${datadir}/gpio-tool/examples/
    fi
}

# Package splitting
PACKAGES =+ "${PN}-python ${PN}-examples"

FILES:${PN} = " \
    ${bindir}/gpio-tool \
    ${libdir}/libjetson-gpio.so.* \
    ${sysconfdir}/udev/rules.d/60-gpio-permissions.rules \
    ${sysconfdir}/gpio-tool \
"

FILES:${PN}-dev = " \
    ${includedir}/jetson-gpio.h \
    ${libdir}/libjetson-gpio.so \
    ${libdir}/pkgconfig/jetson-gpio.pc \
"

FILES:${PN}-python = " \
    ${PYTHON_SITEPACKAGES_DIR}/jetson_gpio \
"

FILES:${PN}-examples = " \
    ${datadir}/gpio-tool/examples \
"

# Example udev rules file (60-gpio-permissions.rules):
#
# # GPIO chip access for non-root users
# SUBSYSTEM=="gpio", KERNEL=="gpiochip*", GROUP="gpio", MODE="0660"
#
# # Specific GPIO lines (Jetson Orin)
# SUBSYSTEM=="gpio", KERNEL=="gpiochip0", TAG+="uaccess"
#
# # Allow gpio group members to access GPIO
# KERNEL=="gpio*", SUBSYSTEM=="gpio", GROUP="gpio", MODE="0660"

# Example configuration file (gpio-tool.conf):
#
# # Jetson GPIO Tool Configuration
# [gpio]
# chip = /dev/gpiochip0
# default_direction = input
# default_active = high
#
# [tegra]
# soc = tegra234
# port_naming = tegra
#
# [security]
# allow_export = true
# require_group = gpio

# Example usage on target:
#
# # Using gpio-tool CLI
# gpio-tool list                          # List all GPIO chips
# gpio-tool info PQ.06                    # Get info for GPIO PQ.06
# gpio-tool get PQ.06                     # Read GPIO value
# gpio-tool set PQ.06 1                   # Set GPIO high
# gpio-tool set PQ.06 0                   # Set GPIO low
# gpio-tool monitor PQ.06                 # Monitor GPIO for changes
# gpio-tool export PQ.06 output           # Export GPIO as output
#
# # Using Python bindings
# python3 << EOF
# from jetson_gpio import GPIO
#
# # Setup GPIO
# gpio = GPIO('PQ.06', direction='output')
# gpio.set(1)  # Set high
# gpio.set(0)  # Set low
#
# # Read input
# button = GPIO('PQ.05', direction='input')
# state = button.get()
# print(f"Button state: {state}")
#
# # Interrupt handling
# def callback(gpio, value):
#     print(f"GPIO changed to {value}")
#
# button.on_change(callback)
# EOF

# Integration with libgpiod command-line tools:
# gpiodetect                              # List GPIO chips
# gpioinfo gpiochip0                      # Show chip info
# gpioget gpiochip0 448                   # Read GPIO 448 (PQ.06)
# gpioset gpiochip0 448=1                 # Set GPIO 448 high
# gpiomon gpiochip0 448                   # Monitor GPIO 448

# Comparison: sysfs (deprecated) vs libgpiod (modern)
#
# Deprecated sysfs method:
# echo 448 > /sys/class/gpio/export
# echo out > /sys/class/gpio/gpio448/direction
# echo 1 > /sys/class/gpio/gpio448/value
# echo 448 > /sys/class/gpio/unexport
#
# Modern libgpiod method:
# gpioset gpiochip0 448=1
#
# Advantages of libgpiod:
# 1. Atomic operations (no race conditions)
# 2. Better error handling
# 3. Character device interface (cleaner)
# 4. Future-proof (sysfs GPIO is deprecated)
# 5. Supports GPIO attributes (bias, drive strength, etc.)
