# interview-common.bbclass - Common functionality for interview study recipes
#
# This bbclass provides shared functionality used across multiple recipes
# in the meta-interview-study layer, including logging, validation, and
# common build patterns.
#
# Learning objectives:
# 1. Understand bbclass creation and inheritance
# 2. Learn shared function definition patterns
# 3. Practice task hook customization
# 4. Handle common build patterns centrally

# Common variables used across interview study recipes
INTERVIEW_STUDY_LAYER = "meta-interview-study"
INTERVIEW_STUDY_VERSION = "1.0"

# Logging helper functions
def interview_log(d, level, message):
    """
    Standardized logging for interview study recipes
    Usage: interview_log(d, "info", "Building recipe...")
    """
    import bb
    recipe_name = d.getVar('PN') or 'unknown'
    formatted_msg = f"[{recipe_name}] {message}"

    if level == "debug":
        bb.debug(1, formatted_msg)
    elif level == "note":
        bb.note(formatted_msg)
    elif level == "warn":
        bb.warn(formatted_msg)
    elif level == "error":
        bb.error(formatted_msg)
    elif level == "fatal":
        bb.fatal(formatted_msg)
    else:
        bb.note(formatted_msg)

# Validation helper
def validate_machine_feature(d, feature):
    """
    Check if machine has required feature
    Usage: validate_machine_feature(d, 'cuda')
    """
    import bb
    machine_features = (d.getVar('MACHINE_FEATURES') or '').split()
    if feature not in machine_features:
        bb.fatal(f"Machine {d.getVar('MACHINE')} does not support {feature}")

# Check for CUDA support
def check_cuda_support(d):
    """Verify CUDA is available for GPU recipes"""
    import bb
    machine_features = (d.getVar('MACHINE_FEATURES') or '').split()
    if 'cuda' not in machine_features:
        bb.fatal("CUDA support required but not available on this machine")

# Version comparison helper
def version_greater_than(v1, v2):
    """Compare two version strings"""
    from packaging import version
    return version.parse(v1) > version.parse(v2)

# Common task: Display build configuration
python do_show_config() {
    """Display recipe configuration before build"""
    pn = d.getVar('PN')
    pv = d.getVar('PV')
    machine = d.getVar('MACHINE')

    bb.plain("=" * 60)
    bb.plain(f"Building: {pn} version {pv}")
    bb.plain(f"Machine: {machine}")
    bb.plain(f"Source: {d.getVar('S')}")
    bb.plain(f"Build: {d.getVar('B')}")
    bb.plain("=" * 60)
}

# Common task: Validate dependencies
python do_validate_deps() {
    """Validate runtime dependencies are available"""
    import bb.utils

    rdeps = d.getVar('RDEPENDS:' + d.getVar('PN')) or ''
    missing_deps = []

    for dep in rdeps.split():
        # Check if dependency package exists
        # This is simplified - actual check would query package database
        pass

    if missing_deps:
        bb.warn(f"Missing dependencies: {', '.join(missing_deps)}")
}

# Hook into compile task
do_compile[prefuncs] += "do_show_config"

# Common CUDA compilation function
interview_cuda_compile() {
    # Common CUDA compilation flags
    CUDA_FLAGS="-arch=${CUDA_ARCH} --compiler-options '${CXXFLAGS}'"

    # Set CUDA paths
    export CUDA_PATH="${STAGING_DIR_HOST}/usr/local/cuda"
    export CUDA_HOME="${CUDA_PATH}"

    bbnote "Compiling CUDA code with arch=${CUDA_ARCH}"

    # Compile is done by inheriting recipe
}

# Common TensorRT build setup
interview_tensorrt_setup() {
    export TENSORRT_ROOT="${STAGING_DIR_HOST}/usr"
    export TRT_LIB_DIR="${TENSORRT_ROOT}/lib"
    export TRT_INC_DIR="${TENSORRT_ROOT}/include"

    bbnote "TensorRT configured: ${TENSORRT_ROOT}"
}

# Common installation helper
interview_install_service() {
    # $1 = service file name
    # $2 = enable/disable
    SERVICE_FILE="$1"
    AUTO_ENABLE="${2:-enable}"

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/${SERVICE_FILE} \
        ${D}${systemd_system_unitdir}/

    bbnote "Installed systemd service: ${SERVICE_FILE}"
}

# Common Python package installation
interview_python_install() {
    # Install Python package to site-packages
    PACKAGE_DIR="$1"

    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -r ${S}/${PACKAGE_DIR} ${D}${PYTHON_SITEPACKAGES_DIR}/

    bbnote "Installed Python package: ${PACKAGE_DIR}"
}

# Common configuration file installation
interview_install_config() {
    # $1 = config file
    # $2 = destination directory
    CONFIG_FILE="$1"
    DEST_DIR="${2:-${sysconfdir}/${PN}}"

    install -d ${D}${DEST_DIR}
    install -m 0644 ${WORKDIR}/${CONFIG_FILE} ${D}${DEST_DIR}/

    bbnote "Installed config: ${CONFIG_FILE} -> ${DEST_DIR}"
}

# Common udev rules installation
interview_install_udev_rules() {
    # $1 = rules file
    RULES_FILE="$1"

    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${WORKDIR}/${RULES_FILE} \
        ${D}${sysconfdir}/udev/rules.d/

    bbnote "Installed udev rules: ${RULES_FILE}"
}

# Performance monitoring task
python do_benchmark() {
    """Optional task to benchmark build performance"""
    import time
    import bb

    start_time = time.time()
    bb.note("Starting benchmark...")

    # Benchmark code would go here

    elapsed = time.time() - start_time
    bb.note(f"Build completed in {elapsed:.2f} seconds")
}

# Cleanup helper
interview_cleanup() {
    # Remove temporary build artifacts
    rm -rf ${B}/*.o ${B}/*.a ${B}/.cache
    bbnote "Cleaned up build artifacts"
}

# Error handler
python interview_error_handler() {
    """Custom error handler for better debugging"""
    import bb
    import sys
    import traceback

    exc_type, exc_value, exc_traceback = sys.exc_info()
    if exc_type:
        bb.error("Build failed with exception:")
        bb.error(f"  Type: {exc_type.__name__}")
        bb.error(f"  Message: {str(exc_value)}")
        bb.error("  Traceback:")
        for line in traceback.format_tb(exc_traceback):
            bb.error(f"    {line.strip()}")
}

# Register error handler
# BBINCLUDELOGS_LINES ??= "200"

# Common license files
COMMON_LICENSE_FILES = " \
    ${COMMON_LICENSE_DIR}/MIT \
    ${COMMON_LICENSE_DIR}/GPL-2.0-only \
    ${COMMON_LICENSE_DIR}/Apache-2.0 \
"

# Default package groups for interview study packages
INTERVIEW_PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

# Usage in recipes:
# inherit interview-common
#
# Then use functions like:
# do_install:append() {
#     interview_install_config "myapp.conf"
#     interview_install_service "myapp.service" "enable"
# }

# Example: CUDA recipe using this class
# inherit interview-common cuda
# do_compile() {
#     interview_cuda_compile
#     ${CUDA_PATH}/bin/nvcc ${CUDA_FLAGS} -o myapp myapp.cu
# }

# Example: Python recipe using this class
# inherit interview-common python3native
# do_install:append() {
#     interview_python_install "mypackage"
# }
