# interview-study-image.bb - Main image recipe for interview study layer
#
# Comprehensive embedded Linux image tailored for Jetson development
# and interview preparation. Optimized for AI/ML workloads.

require recipes/custom-image.bb

SUMMARY = "Interview Study Development Image for Jetson Orin"
DESCRIPTION = "Complete development environment including CUDA, TensorRT, \
development tools, and custom applications for embedded Linux interview preparation."

# Override base image settings
IMAGE_INSTALL:append = " \
    packagegroup-interview-study \
    packagegroup-interview-study-dev-tools \
    packagegroup-interview-study-gpu \
    packagegroup-interview-study-ai-ml \
    packagegroup-interview-study-custom-apps \
"

# Additional development tools
IMAGE_INSTALL:append = " \
    cmake \
    ninja \
    ccache \
    distcc \
"

# Enable all features for development
IMAGE_FEATURES:append = " \
    dev-pkgs \
    dbg-pkgs \
    doc \
"

# Increase rootfs size for development environment
IMAGE_ROOTFS_SIZE = "20971520"  # 20GB
IMAGE_ROOTFS_EXTRA_SPACE = "2097152"  # 2GB extra
