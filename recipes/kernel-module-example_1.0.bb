# kernel-module-example_1.0.bb - Out-of-tree kernel module recipe
#
# This recipe demonstrates:
# - Building loadable kernel modules (.ko files)
# - Kernel module class (module.bbclass) usage
# - Proper kernel headers dependency management
# - Module auto-loading configuration
# - Module parameters and modprobe configuration
#
# Learning objectives:
# 1. Understand module class and kernel build system integration
# 2. Learn kernel module versioning and symbol dependencies
# 3. Practice module installation and auto-loading setup
# 4. Handle kernel-version-specific modules

SUMMARY = "Example out-of-tree kernel module"
DESCRIPTION = "Educational kernel module demonstrating character device driver \
implementation, ioctl interface, and proper Linux kernel coding practices. \
This module creates a /dev/example device node for userspace interaction."

LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=12f884d2ae1ff87c09e5b7ccc2c4ca7e"

# Source location
SRC_URI = " \
    file://Makefile \
    file://example_module.c \
    file://example_module.h \
    file://COPYING \
    file://99-example-module.rules \
"

S = "${WORKDIR}"

# Inherit module class - provides:
# - do_compile: builds kernel module using kernel build system
# - do_install: installs .ko file to /lib/modules/${KERNEL_VERSION}/extra/
# - Automatic kernel version dependency
# - Sets up proper kernel source directory (STAGING_KERNEL_DIR)
inherit module

# DEPENDS: Kernel module recipes ALWAYS depend on virtual/kernel
# This ensures kernel is built before the module
DEPENDS = "virtual/kernel"

# RDEPENDS: Runtime dependency on kernel module infrastructure
# This is typically implicit, but can be explicit:
# RDEPENDS:${PN} = "kernel-module-<dependency>"

# Module-specific variables
MODULE_NAME = "example_module"
MODULE_VERSION = "${PV}"

# KERNEL_MODULE_AUTOLOAD: Modules to auto-load at boot
# Adds module name to /etc/modules-load.d/
KERNEL_MODULE_AUTOLOAD += "${MODULE_NAME}"

# KERNEL_MODULE_PROBECONF: Module parameters configuration
# Creates /etc/modprobe.d/ entry with module parameters
KERNEL_MODULE_PROBECONF += "${MODULE_NAME}"
module_conf_${MODULE_NAME} = "options ${MODULE_NAME} debug=1 buffer_size=4096"

# Compatible machines (kernel modules are machine-specific)
# Uncomment and adjust for specific hardware:
# COMPATIBLE_MACHINE = "(jetson-orin|qemux86-64|raspberrypi4)"

# Module must match kernel architecture
PACKAGE_ARCH = "${MACHINE_ARCH}"

# do_compile task - build the kernel module
# The module class handles this automatically by calling:
# make -C ${STAGING_KERNEL_DIR} M=${S} modules
# We can customize if needed:
do_compile() {
    # Ensure kernel source is prepared
    if [ ! -d ${STAGING_KERNEL_DIR} ]; then
        bbfatal "Kernel source directory not found: ${STAGING_KERNEL_DIR}"
    fi

    # Build module with verbose output
    oe_runmake \
        KERNELDIR=${STAGING_KERNEL_DIR} \
        KERNEL_SRC=${STAGING_KERNEL_DIR} \
        ARCH=${ARCH} \
        CC="${KERNEL_CC}" \
        LD="${KERNEL_LD}" \
        AR="${KERNEL_AR}" \
        O=${STAGING_KERNEL_BUILDDIR} \
        KBUILD_EXTRA_SYMBOLS="${KBUILD_EXTRA_SYMBOLS}" \
        modules
}

# do_install task - install module and configuration files
do_install() {
    # Install kernel module to standard location
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra
    install -m 0644 ${S}/${MODULE_NAME}.ko \
        ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/

    # Install udev rules for device node creation
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${WORKDIR}/99-example-module.rules \
        ${D}${sysconfdir}/udev/rules.d/

    # Install module configuration
    install -d ${D}${sysconfdir}/modprobe.d
    echo "# Example module parameters" > ${D}${sysconfdir}/modprobe.d/${MODULE_NAME}.conf
    echo "options ${MODULE_NAME} debug=1" >> ${D}${sysconfdir}/modprobe.d/${MODULE_NAME}.conf
}

# Package files
FILES:${PN} = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/${MODULE_NAME}.ko \
    ${sysconfdir}/udev/rules.d/99-example-module.rules \
    ${sysconfdir}/modprobe.d/${MODULE_NAME}.conf \
    ${sysconfdir}/modules-load.d/${MODULE_NAME}.conf \
"

# Module dependencies - if this module depends on other kernel modules
# RDEPENDS:${PN} += "kernel-module-other-module"

# Example Makefile that works with this recipe:
#
# # Makefile for example kernel module
# obj-m := example_module.o
# example_module-objs := example_main.o example_ioctl.o
#
# SRC := $(shell pwd)
#
# all:
# 	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) modules
#
# modules_install:
# 	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) modules_install
#
# clean:
# 	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) clean

# Example module source (example_module.c):
#
# #include <linux/module.h>
# #include <linux/kernel.h>
# #include <linux/init.h>
# #include <linux/fs.h>
# #include <linux/device.h>
# #include <linux/uaccess.h>
#
# #define DEVICE_NAME "example"
# #define CLASS_NAME "example_class"
#
# MODULE_LICENSE("GPL");
# MODULE_AUTHOR("Your Name");
# MODULE_DESCRIPTION("Example kernel module for Yocto");
# MODULE_VERSION("1.0");
#
# static int majorNumber;
# static struct class* exampleClass = NULL;
# static struct device* exampleDevice = NULL;
#
# static int dev_open(struct inode *inodep, struct file *filep) {
#     pr_info("example: Device opened\n");
#     return 0;
# }
#
# static ssize_t dev_read(struct file *filep, char *buffer,
#                         size_t len, loff_t *offset) {
#     pr_info("example: Device read\n");
#     return 0;
# }
#
# static ssize_t dev_write(struct file *filep, const char *buffer,
#                          size_t len, loff_t *offset) {
#     pr_info("example: Device write\n");
#     return len;
# }
#
# static int dev_release(struct inode *inodep, struct file *filep) {
#     pr_info("example: Device closed\n");
#     return 0;
# }
#
# static struct file_operations fops = {
#     .open = dev_open,
#     .read = dev_read,
#     .write = dev_write,
#     .release = dev_release,
# };
#
# static int __init example_init(void) {
#     majorNumber = register_chrdev(0, DEVICE_NAME, &fops);
#     if (majorNumber < 0) {
#         pr_err("example: Failed to register device\n");
#         return majorNumber;
#     }
#
#     exampleClass = class_create(THIS_MODULE, CLASS_NAME);
#     exampleDevice = device_create(exampleClass, NULL,
#                                   MKDEV(majorNumber, 0), NULL, DEVICE_NAME);
#
#     pr_info("example: Module loaded, major number %d\n", majorNumber);
#     return 0;
# }
#
# static void __exit example_exit(void) {
#     device_destroy(exampleClass, MKDEV(majorNumber, 0));
#     class_destroy(exampleClass);
#     unregister_chrdev(majorNumber, DEVICE_NAME);
#     pr_info("example: Module unloaded\n");
# }
#
# module_init(example_init);
# module_exit(example_exit);

# Example udev rules file (99-example-module.rules):
# KERNEL=="example", MODE="0666", GROUP="users"
# ACTION=="add", KERNEL=="example", RUN+="/bin/sh -c 'echo example module loaded > /dev/kmsg'"

# Testing the module on target:
# modprobe example_module               # Load module
# lsmod | grep example                  # Verify loaded
# dmesg | tail                          # Check kernel messages
# ls -l /dev/example                    # Verify device node
# cat /sys/module/example_module/parameters/debug  # Check parameters
# modprobe -r example_module            # Unload module
