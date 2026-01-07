# custom-image.bb - Complete custom image recipe for Jetson Orin
#
# This recipe demonstrates:
# - Image creation from scratch using core-image class
# - Package group inclusion for organized dependencies
# - Image features and customization hooks
# - Post-installation scripts and image configurations
# - Multiple image variants (minimal, dev, production)
#
# Learning objectives:
# 1. Understand image recipe structure and inheritance
# 2. Learn IMAGE_* variables for customization
# 3. Practice packagegroup usage for dependency management
# 4. Handle image post-processing and filesystem customization

SUMMARY = "Custom Jetson Orin development and production image"
DESCRIPTION = "Comprehensive embedded Linux image for NVIDIA Jetson Orin \
platforms, optimized for AI/ML workloads, GPU computing, and hardware development. \
Includes CUDA, TensorRT, development tools, and custom applications."

LICENSE = "MIT"

# Inherit core-image class - provides:
# - Basic image structure and variables
# - do_rootfs task for filesystem creation
# - Package installation framework
# - Image type handling (ext4, tar.gz, etc.)
inherit core-image

# Alternative base classes:
# inherit core-image-minimal  # Minimal base
# inherit core-image-full-cmdline  # Full command-line tools

# Image features - pre-defined package groups
# Available features: ssh-server-openssh, package-management, debug-tweaks, etc.
IMAGE_FEATURES += " \
    ssh-server-openssh \
    package-management \
    hwcodecs \
    tools-debug \
    tools-profile \
    splash \
    dev-pkgs \
"

# debug-tweaks feature includes:
# - Root login without password
# - SSH root login enabled
# - Post-installation scripts disabled
# REMOVE for production images!
IMAGE_FEATURES += "debug-tweaks"

# Package management backends
# Options: rpm, deb, ipk (opkg)
PACKAGE_CLASSES = "package_deb"

# Additional package groups to install
IMAGE_INSTALL:append = " \
    packagegroup-interview-study \
    packagegroup-core-boot \
    packagegroup-core-ssh-openssh \
    packagegroup-base-extended \
"

# Development tools
IMAGE_INSTALL:append = " \
    vim \
    nano \
    htop \
    tmux \
    git \
    wget \
    curl \
    rsync \
    strace \
    ltrace \
    gdb \
    gdbserver \
    valgrind \
    perf \
"

# Networking tools
IMAGE_INSTALL:append = " \
    iproute2 \
    iptables \
    net-tools \
    ethtool \
    tcpdump \
    wireshark \
    iperf3 \
    nmap \
    bridge-utils \
    wireless-tools \
    wpa-supplicant \
"

# System utilities
IMAGE_INSTALL:append = " \
    systemd \
    systemd-analyze \
    udev \
    util-linux \
    procps \
    psmisc \
    coreutils \
    findutils \
    grep \
    sed \
    gawk \
    diffutils \
    tar \
    gzip \
    bzip2 \
    xz \
"

# CUDA and GPU packages
IMAGE_INSTALL:append = " \
    cuda-toolkit \
    cuda-libraries \
    cudnn \
    tensorrt \
    tensorrt-libs \
    libcudart \
    libcublas \
    libcufft \
    libcurand \
    libcusparse \
    libcusolver \
    libnpp \
"

# AI/ML frameworks
IMAGE_INSTALL:append = " \
    python3-pytorch \
    python3-tensorflow \
    python3-onnx \
    python3-numpy \
    python3-scipy \
    python3-opencv \
    python3-pillow \
    python3-matplotlib \
"

# Custom applications from this layer
IMAGE_INSTALL:append = " \
    hello-world \
    hello-cmake \
    python-example \
    kernel-module-example \
    systemd-service \
    gpio-tool \
    gpio-tool-python \
    cuda-sample \
    tensorrt-app \
"

# Hardware support
IMAGE_INSTALL:append = " \
    libgpiod \
    libgpiod-tools \
    i2c-tools \
    spi-tools \
    can-utils \
    v4l-utils \
    alsa-utils \
"

# Multimedia support
IMAGE_INSTALL:append = " \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-ugly \
    gstreamer1.0-plugins-nvvideoconvert \
    gstreamer1.0-plugins-nvarguscamerasrc \
    ffmpeg \
"

# Container support (optional)
# IMAGE_INSTALL:append = " \
#     docker \
#     containerd \
#     runc \
# "

# Root filesystem size
# Default is 8GB, increase for development images
IMAGE_ROOTFS_SIZE ?= "16777216"  # 16GB in KB

# Extra space added to rootfs
IMAGE_ROOTFS_EXTRA_SPACE = "1048576"  # 1GB extra

# Image filesystem types to generate
# Options: ext2, ext3, ext4, btrfs, squashfs, tar.gz, cpio.gz, etc.
IMAGE_FSTYPES = "ext4 tar.gz wic.gz"

# Bootloader configuration (for complete image with bootloader)
# IMAGE_FSTYPES += "wic.bmap wic.gz"
# WKS_FILE = "jetson-orin.wks"

# Image name customization
IMAGE_NAME = "interview-study-image-${MACHINE}-${DATETIME}"
IMAGE_NAME[vardepsexclude] = "DATETIME"

# Extra users/groups to create
# Format: <username>:<password>:<uid>:<gid>:<comment>:<home>:<shell>
EXTRA_USERS_PARAMS = " \
    useradd -p '' -G sudo,gpio,video,audio developer; \
    useradd -p '' -G video,audio tensorrt; \
"

# Hostname
hostname:pn-base-files = "jetson-orin-dev"

# Timezone
DEFAULT_TIMEZONE = "America/Los_Angeles"

# Locale
IMAGE_LINGUAS = "en-us"

# Image post-processing hooks

# ROOTFS_POSTPROCESS_COMMAND runs after rootfs creation
ROOTFS_POSTPROCESS_COMMAND:append = " custom_image_postprocess; "

custom_image_postprocess() {
    # Create additional directories
    install -d ${IMAGE_ROOTFS}/opt/jetson-apps
    install -d ${IMAGE_ROOTFS}/data
    install -d ${IMAGE_ROOTFS}/models

    # Install custom configuration files
    echo "# Custom image configuration" > ${IMAGE_ROOTFS}/etc/custom-image.conf
    echo "VERSION=${PV}" >> ${IMAGE_ROOTFS}/etc/custom-image.conf
    echo "BUILD_DATE=$(date +'%Y-%m-%d %H:%M:%S')" >> ${IMAGE_ROOTFS}/etc/custom-image.conf

    # Set up CUDA environment
    echo 'export CUDA_HOME=/usr/local/cuda' >> ${IMAGE_ROOTFS}/etc/profile.d/cuda.sh
    echo 'export PATH=$CUDA_HOME/bin:$PATH' >> ${IMAGE_ROOTFS}/etc/profile.d/cuda.sh
    echo 'export LD_LIBRARY_PATH=$CUDA_HOME/lib64:$LD_LIBRARY_PATH' >> ${IMAGE_ROOTFS}/etc/profile.d/cuda.sh

    # Configure GPU power mode for development
    if [ -d ${IMAGE_ROOTFS}/etc/systemd/system ]; then
        cat > ${IMAGE_ROOTFS}/etc/systemd/system/set-gpu-power.service << 'EOF'
[Unit]
Description=Set GPU to maximum performance mode
After=nvidia-l4t-core.service

[Service]
Type=oneshot
ExecStart=/usr/bin/jetson_clocks

[Install]
WantedBy=multi-user.target
EOF
    fi

    # Set up model cache directory
    install -d ${IMAGE_ROOTFS}/var/cache/models
    chmod 777 ${IMAGE_ROOTFS}/var/cache/models

    # Create README for users
    cat > ${IMAGE_ROOTFS}/root/README.txt << 'EOF'
Welcome to Interview Study Jetson Image!

This image includes:
- CUDA Toolkit and TensorRT for GPU acceleration
- Python ML frameworks (PyTorch, TensorFlow)
- Development tools and utilities
- Custom GPIO and hardware control tools

Quick start:
1. Network: nmtui (NetworkManager TUI)
2. GPU status: nvidia-smi
3. System stats: tegrastats
4. Run samples: cuda-sample, tensorrt-app

Documentation: /usr/share/doc/
Examples: /opt/jetson-apps/

Default users:
- root (no password) - FOR DEVELOPMENT ONLY
- developer (no password) - Regular user with sudo

SECURITY: Change passwords immediately for production use!
EOF

    # Remove documentation (for minimal images)
    # rm -rf ${IMAGE_ROOTFS}/usr/share/doc
    # rm -rf ${IMAGE_ROOTFS}/usr/share/man
    # rm -rf ${IMAGE_ROOTFS}/usr/share/info
}

# IMAGE_POSTPROCESS_COMMAND runs after image file creation
IMAGE_POSTPROCESS_COMMAND:append = " custom_image_postinstall; "

custom_image_postinstall() {
    # Generate image manifest
    cat > ${IMGDEPLOYDIR}/${IMAGE_NAME}.manifest << EOF
Image: ${IMAGE_NAME}
Machine: ${MACHINE}
Build Date: $(date +'%Y-%m-%d %H:%M:%S')
Yocto Version: ${DISTRO_VERSION}

Installed Packages:
$(cat ${IMAGE_ROOTFS}/var/lib/dpkg/status | grep "^Package:" | cut -d' ' -f2 | sort)
EOF

    # Create checksum
    cd ${IMGDEPLOYDIR}
    sha256sum ${IMAGE_NAME}.rootfs.ext4 > ${IMAGE_NAME}.rootfs.ext4.sha256sum
}

# License manifest for compliance
COPY_LIC_MANIFEST = "1"
COPY_LIC_DIRS = "1"

# SDK generation (for cross-development)
# bitbake custom-image -c populate_sdk
inherit populate_sdk

# SDK includes development headers and cross-toolchain
# TOOLCHAIN_HOST_TASK: Tools that run on build host
TOOLCHAIN_HOST_TASK:append = " \
    nativesdk-cmake \
    nativesdk-make \
    nativesdk-python3 \
"

# TOOLCHAIN_TARGET_TASK: Libraries for target
TOOLCHAIN_TARGET_TASK:append = " \
    cuda-toolkit-dev \
    tensorrt-dev \
    opencv-dev \
"

# Image variants - create multiple images from one recipe

# Minimal image variant
IMAGE_FEATURES:remove:pn-custom-image-minimal = "debug-tweaks dev-pkgs"
IMAGE_INSTALL:remove:pn-custom-image-minimal = "gdb valgrind"

# Production image variant
IMAGE_FEATURES:remove:pn-custom-image-production = "debug-tweaks"
EXTRA_USERS_PARAMS:pn-custom-image-production = ""

# Testing and validation
# inherit testimage
# TEST_SUITES = "ping ssh parselogs"

# Image deployment
# inherit image-buildinfo
# inherit image-artifact-names

# Performance optimization
# RM_OLD_IMAGE = "1"  # Remove old images
# IMAGE_PREPROCESS_COMMAND += "prelink_image; "  # Prelink for faster startup

# Common image debugging commands:
# bitbake custom-image                    # Build image
# bitbake custom-image -c rootfs          # Rebuild rootfs only
# bitbake custom-image -c populate_sdk    # Build SDK
# bitbake custom-image -c testimage       # Run tests
# bitbake -g custom-image                 # Generate dependency graph
# bitbake-layers show-recipes "*image*"   # List all image recipes
