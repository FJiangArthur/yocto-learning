# systemd-service_1.0.bb - Recipe with systemd service integration
#
# This recipe demonstrates:
# - systemd class integration for service management
# - Service file installation and activation
# - tmpfiles.d for runtime directory creation
# - Service dependencies and ordering
# - D-Bus service integration (optional)
#
# Learning objectives:
# 1. Understand systemd class and SYSTEMD_* variables
# 2. Learn proper service file writing and installation
# 3. Practice runtime directory management with tmpfiles.d
# 4. Handle service enable/disable logic

SUMMARY = "Example daemon with systemd service"
DESCRIPTION = "Educational example demonstrating systemd integration in Yocto. \
Shows how to create a background daemon, write systemd service files, and \
properly integrate with the systemd initialization system."

HOMEPAGE = "https://github.com/example/systemd-service-example"
SECTION = "base"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    git://github.com/example/daemon-example.git;protocol=https;branch=main \
    file://example-daemon.service \
    file://example-daemon.socket \
    file://example-daemon.tmpfiles \
    file://example-daemon.conf \
"

SRCREV = "1234567890abcdef1234567890abcdef12345678"

S = "${WORKDIR}/git"

# Inherit systemd class - provides:
# - SYSTEMD_* variables for service management
# - do_install hooks for service file installation
# - pkg_postinst/pkg_prerm hooks for systemctl enable/disable
inherit systemd

# Build dependencies
DEPENDS = "systemd"

# Runtime dependencies
# systemd is automatically added by systemd class
RDEPENDS:${PN} = "systemd"

# systemd configuration variables

# SYSTEMD_SERVICE: Service files to install and manage
# Multiple services can be space-separated
SYSTEMD_SERVICE:${PN} = "example-daemon.service"

# SYSTEMD_AUTO_ENABLE: Auto-enable service at boot
# Options: "enable" (default), "disable"
SYSTEMD_AUTO_ENABLE = "enable"

# Alternative: different per package
# SYSTEMD_AUTO_ENABLE:${PN} = "disable"
# SYSTEMD_AUTO_ENABLE:${PN}-watcher = "enable"

# do_compile - build the daemon
do_compile() {
    # Simple C daemon
    ${CC} ${CFLAGS} ${LDFLAGS} -o example-daemon ${S}/src/daemon.c

    # Or for autotools/cmake, inherited class handles it automatically
}

# do_install - install binary and systemd files
do_install() {
    # Install daemon binary
    install -d ${D}${bindir}
    install -m 0755 ${S}/example-daemon ${D}${bindir}/

    # Install systemd service file
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/example-daemon.service ${D}${systemd_system_unitdir}/

    # Install systemd socket file (for socket activation)
    install -m 0644 ${WORKDIR}/example-daemon.socket ${D}${systemd_system_unitdir}/

    # Install tmpfiles.d for runtime directory creation
    # Creates directories at boot before service starts
    install -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 ${WORKDIR}/example-daemon.tmpfiles \
        ${D}${sysconfdir}/tmpfiles.d/example-daemon.conf

    # Install daemon configuration
    install -d ${D}${sysconfdir}/example-daemon
    install -m 0644 ${WORKDIR}/example-daemon.conf ${D}${sysconfdir}/example-daemon/

    # Create state directory (alternative to tmpfiles.d)
    install -d ${D}${localstatedir}/lib/example-daemon
    install -d ${D}${localstatedir}/log/example-daemon
}

# Package files
FILES:${PN} = " \
    ${bindir}/example-daemon \
    ${systemd_system_unitdir}/example-daemon.service \
    ${systemd_system_unitdir}/example-daemon.socket \
    ${sysconfdir}/tmpfiles.d/example-daemon.conf \
    ${sysconfdir}/example-daemon \
    ${localstatedir}/lib/example-daemon \
    ${localstatedir}/log/example-daemon \
"

# Example systemd service file (example-daemon.service):
#
# [Unit]
# Description=Example Daemon Service
# Documentation=https://github.com/example/daemon-example
# After=network.target
# Wants=network-online.target
# Before=multi-user.target
#
# [Service]
# Type=notify
# # Type options: simple, forking, oneshot, dbus, notify, idle
#
# # User and group
# User=daemon
# Group=daemon
#
# # Execution
# ExecStart=/usr/bin/example-daemon --config /etc/example-daemon/daemon.conf
# ExecReload=/bin/kill -HUP $MAINPID
# Restart=on-failure
# RestartSec=5s
#
# # Security hardening
# PrivateTmp=yes
# NoNewPrivileges=yes
# ProtectSystem=strict
# ProtectHome=yes
# ReadWritePaths=/var/lib/example-daemon /var/log/example-daemon
#
# # Resource limits
# LimitNOFILE=65536
# LimitNPROC=512
#
# # Standard output/error
# StandardOutput=journal
# StandardError=journal
# SyslogIdentifier=example-daemon
#
# [Install]
# WantedBy=multi-user.target

# Example socket file (example-daemon.socket):
#
# [Unit]
# Description=Example Daemon Socket
# Before=example-daemon.service
#
# [Socket]
# ListenStream=/run/example-daemon/daemon.sock
# SocketMode=0660
# SocketUser=daemon
# SocketGroup=daemon
# Accept=no
#
# [Install]
# WantedBy=sockets.target

# Example tmpfiles.d file (example-daemon.tmpfiles):
#
# # Type Path                    Mode UID     GID     Age Argument
# d      /run/example-daemon     0755 daemon  daemon  -   -
# d      /var/lib/example-daemon 0755 daemon  daemon  -   -
# d      /var/log/example-daemon 0755 daemon  daemon  -   -

# Multiple services in one recipe
# SYSTEMD_SERVICE:${PN} = "service1.service service2.service"

# Different services for different packages
# PACKAGES = "${PN}-daemon ${PN}-watcher"
# SYSTEMD_SERVICE:${PN}-daemon = "example-daemon.service"
# SYSTEMD_SERVICE:${PN}-watcher = "example-watcher.service"

# Custom postinst/prerm scripts
# pkg_postinst_ontarget:${PN}() {
#     # Run after package installation on target
#     systemctl daemon-reload
#     if systemctl is-enabled ${SYSTEMD_SERVICE}; then
#         systemctl restart ${SYSTEMD_SERVICE}
#     fi
# }
#
# pkg_prerm:${PN}() {
#     # Run before package removal
#     systemctl stop ${SYSTEMD_SERVICE} || true
# }

# Testing on target:
# systemctl status example-daemon         # Check service status
# systemctl start example-daemon          # Start service
# systemctl stop example-daemon           # Stop service
# systemctl restart example-daemon        # Restart service
# systemctl enable example-daemon         # Enable at boot
# systemctl disable example-daemon        # Disable at boot
# journalctl -u example-daemon -f         # Follow service logs
# systemctl cat example-daemon            # View service file
# systemctl show example-daemon           # Show all properties
# systemd-analyze verify example-daemon.service  # Validate service file
