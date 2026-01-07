# packagegroup-interview-study.bb - Package group for interview study packages
#
# This recipe demonstrates:
# - Package group organization for logical grouping
# - Conditional package inclusion based on machine features
# - RDEPENDS vs RRECOMMENDS for package dependencies
# - Multiple packagegroup variants for different use cases
#
# Learning objectives:
# 1. Understand packagegroups for dependency management
# 2. Learn conditional package inclusion patterns
# 3. Practice organizing packages by functional categories
# 4. Handle optional vs required dependencies

SUMMARY = "Interview Study Package Group"
DESCRIPTION = "Organized collection of packages for Jetson development, AI/ML \
workloads, and embedded systems learning. Provides logical grouping of tools, \
libraries, and applications for different development scenarios."

LICENSE = "MIT"

# Package groups don't build anything, just declare dependencies
inherit packagegroup

# Main package group
PACKAGES = " \
    ${PN} \
    ${PN}-base \
    ${PN}-dev-tools \
    ${PN}-gpu \
    ${PN}-ai-ml \
    ${PN}-hardware \
    ${PN}-networking \
    ${PN}-multimedia \
    ${PN}-custom-apps \
"

# Base: Essential system packages
# RDEPENDS: Hard dependencies (must be installed)
RDEPENDS:${PN}-base = " \
    packagegroup-core-boot \
    systemd \
    systemd-analyze \
    udev \
    dbus \
    util-linux \
    procps \
    coreutils \
    bash \
    bash-completion \
    less \
    vim \
    nano \
"

# RRECOMMENDS: Soft dependencies (installed if available, skipped if not)
RRECOMMENDS:${PN}-base = " \
    kernel-modules \
    linux-firmware \
"

# Development tools package group
RDEPENDS:${PN}-dev-tools = " \
    ${PN}-base \
    gcc \
    g++ \
    make \
    cmake \
    autoconf \
    automake \
    libtool \
    pkgconfig \
    git \
    git-perltools \
    subversion \
    patch \
    diffutils \
    wget \
    curl \
    rsync \
"

# Development utilities
RDEPENDS:${PN}-dev-tools += " \
    htop \
    tmux \
    screen \
    tree \
    file \
    which \
    sudo \
"

# Debugging tools
RDEPENDS:${PN}-dev-tools += " \
    gdb \
    gdbserver \
    strace \
    ltrace \
    valgrind \
    perf \
    sysstat \
    lsof \
"

# Python development
RDEPENDS:${PN}-dev-tools += " \
    python3 \
    python3-pip \
    python3-setuptools \
    python3-wheel \
    python3-dev \
    python3-debugger \
    python3-pdb \
"

# GPU and CUDA packages
# Conditional on CUDA support
RDEPENDS:${PN}-gpu = ""
RDEPENDS:${PN}-gpu:append:tegra234 = " \
    cuda-toolkit \
    cuda-libraries \
    cudnn \
    tensorrt \
    tensorrt-libs \
"

# CUDA libraries
RDEPENDS:${PN}-gpu:append:tegra234 = " \
    libcudart \
    libcublas \
    libcufft \
    libcurand \
    libcusparse \
    libcusolver \
    libnpp \
    libnvjpeg \
"

# GPU utilities
RDEPENDS:${PN}-gpu:append:tegra234 = " \
    nvidia-smi \
    jetson-stats \
"

# AI/ML frameworks and tools
RDEPENDS:${PN}-ai-ml = " \
    ${PN}-gpu \
    python3-numpy \
    python3-scipy \
    python3-pandas \
    python3-matplotlib \
    python3-pillow \
    python3-opencv \
"

# Deep learning frameworks (conditional - may not be available)
RRECOMMENDS:${PN}-ai-ml = " \
    python3-pytorch \
    python3-tensorflow \
    python3-onnx \
    python3-onnxruntime \
"

# ML utilities
RDEPENDS:${PN}-ai-ml += " \
    python3-scikit-learn \
    python3-jupyter \
    python3-notebook \
"

# Hardware control and interfaces
RDEPENDS:${PN}-hardware = " \
    libgpiod \
    libgpiod-tools \
    i2c-tools \
    spi-tools \
    v4l-utils \
    alsa-utils \
"

# CAN bus support (if available)
RRECOMMENDS:${PN}-hardware = " \
    can-utils \
    libsocketcan \
"

# Serial communication
RDEPENDS:${PN}-hardware += " \
    minicom \
    picocom \
    screen \
"

# USB utilities
RDEPENDS:${PN}-hardware += " \
    usbutils \
    libusb1 \
"

# Networking packages
RDEPENDS:${PN}-networking = " \
    openssh \
    openssh-sftp-server \
    iproute2 \
    iptables \
    net-tools \
    ethtool \
    bridge-utils \
    dhcp-client \
    bind-utils \
"

# Network debugging
RDEPENDS:${PN}-networking += " \
    tcpdump \
    nmap \
    iperf3 \
    netcat \
    socat \
    wireshark \
"

# Wireless support
RRECOMMENDS:${PN}-networking = " \
    wireless-tools \
    wpa-supplicant \
    iw \
    bluez5 \
"

# Network services
RDEPENDS:${PN}-networking += " \
    avahi-daemon \
    ntp \
    chrony \
"

# Multimedia packages
RDEPENDS:${PN}-multimedia = " \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-ugly \
    ffmpeg \
"

# Jetson-specific multimedia (conditional)
RDEPENDS:${PN}-multimedia:append:tegra234 = " \
    gstreamer1.0-plugins-nvvideoconvert \
    gstreamer1.0-plugins-nvarguscamerasrc \
    libnvjpeg \
"

# Image libraries
RDEPENDS:${PN}-multimedia += " \
    libjpeg-turbo \
    libpng \
    libtiff \
    libwebp \
"

# Custom applications from this layer
RDEPENDS:${PN}-custom-apps = " \
    hello-world \
    hello-cmake \
    python-example \
    kernel-module-example \
    systemd-service \
    gpio-tool \
    gpio-tool-python \
"

# GPU applications (conditional)
RDEPENDS:${PN}-custom-apps:append:tegra234 = " \
    cuda-sample \
    tensorrt-app \
"

# Main packagegroup includes all sub-groups
RDEPENDS:${PN} = " \
    ${PN}-base \
    ${PN}-dev-tools \
    ${PN}-hardware \
    ${PN}-networking \
"

# Conditionally add GPU/AI packages for capable machines
RDEPENDS:${PN}:append:tegra234 = " \
    ${PN}-gpu \
    ${PN}-ai-ml \
"

# Optional packages recommended but not required
RRECOMMENDS:${PN} = " \
    ${PN}-multimedia \
    ${PN}-custom-apps \
"

# Machine-specific conditional examples:
#
# For Jetson Orin only:
# RDEPENDS:${PN}:append:jetson-orin-agx = " jetson-orin-specific-pkg"
#
# For any Tegra platform:
# RDEPENDS:${PN}:append:tegra = " tegra-common-pkg"
#
# For machines with GPU support:
# RDEPENDS:${PN}:append = "${@bb.utils.contains('MACHINE_FEATURES', 'gpu', 'gpu-package', '', d)}"
#
# For machines with WiFi:
# RDEPENDS:${PN}:append = "${@bb.utils.contains('MACHINE_FEATURES', 'wifi', 'wpa-supplicant', '', d)}"

# Virtual packages (abstract interfaces):
#
# Virtual kernel:
# RDEPENDS:${PN} += "virtual/kernel"
#
# Virtual bootloader:
# RDEPENDS:${PN} += "virtual/bootloader"
#
# Virtual Java runtime:
# RDEPENDS:${PN} += "virtual/java-runtime"

# Package group best practices:
#
# 1. Use RDEPENDS for required packages
# 2. Use RRECOMMENDS for optional packages
# 3. Create logical sub-groups for organization
# 4. Use machine-specific overrides for hardware-specific packages
# 5. Use MACHINE_FEATURES checks for conditional inclusion
# 6. Avoid circular dependencies
# 7. Keep groups focused and single-purpose
# 8. Document purpose of each sub-group

# Usage in image recipes:
# IMAGE_INSTALL:append = " packagegroup-interview-study"
# or specific subgroups:
# IMAGE_INSTALL:append = " packagegroup-interview-study-dev-tools"
# IMAGE_INSTALL:append = " packagegroup-interview-study-gpu"

# Debugging package groups:
# bitbake-layers show-recipes "packagegroup-*"      # List all packagegroups
# bitbake packagegroup-interview-study -g           # Show dependencies
# bitbake -e packagegroup-interview-study | grep ^RDEPENDS  # Show actual dependencies
