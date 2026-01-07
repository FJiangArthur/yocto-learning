# python-example_1.0.bb - Python package recipe demonstrating setuptools integration
#
# This recipe demonstrates:
# - Python package building with setuptools
# - Python class inheritance (setuptools3)
# - Proper Python dependency management (RDEPENDS)
# - Python runtime selection (python3-native vs python3)
# - Installation into Python site-packages directory
#
# Learning objectives:
# 1. Understand python3-dir and setuptools3 classes
# 2. Learn proper Python dependency specification
# 3. Practice Python package testing integration
# 4. Handle Python bytecode compilation (.pyc files)

SUMMARY = "Example Python package for Yocto"
DESCRIPTION = "Educational Python package demonstrating setuptools integration \
in Yocto recipes. Shows proper dependency management, script installation, \
and Python 3 best practices."

HOMEPAGE = "https://github.com/example/python-example"
SECTION = "devel/python"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

# Source URI for Python packages
SRC_URI = "git://github.com/example/python-example.git;protocol=https;branch=main"
SRCREV = "fedcba9876543210fedcba9876543210fedcba98"

# Alternative: PyPI package
# SRC_URI = "https://files.pythonhosted.org/packages/.../python-example-${PV}.tar.gz"
# SRC_URI[sha256sum] = "abcdef..."

S = "${WORKDIR}/git"

# Inherit setuptools3 class - provides:
# - do_configure: stub (setuptools doesn't need configure step)
# - do_compile: runs python3 setup.py build
# - do_install: runs python3 setup.py install --root=${D}
# Also inherits python3-dir for path variables and python3native
inherit setuptools3

# DISTUTILS_INSTALL_ARGS: Arguments passed to setup.py install
# Default includes --root=${D} and installation prefix
# Uncomment to customize:
# DISTUTILS_INSTALL_ARGS = "--install-lib=${D}${PYTHON_SITEPACKAGES_DIR}"

# Build dependencies (native packages needed during build)
# python3-native: Python interpreter for build host
# python3-setuptools-native: setuptools for build
# These are typically added automatically by setuptools3 class
DEPENDS = " \
    python3-native \
    python3-setuptools-native \
    python3-pip-native \
"

# Runtime dependencies (packages needed on target)
# ${PN} inherits from recipe name (python-example)
# Use python3-<package> for Python runtime dependencies
RDEPENDS:${PN} = " \
    python3-core \
    python3-asyncio \
    python3-json \
    python3-logging \
    python3-requests \
    python3-numpy \
"

# Development package dependencies (for -dev package)
RDEPENDS:${PN}-dev = " \
    python3-setuptools \
    python3-pytest \
"

# do_compile hook - runs Python tests before building
do_compile:prepend() {
    # Run linting and type checking during build
    # Uncomment if tools are available:
    # ${PYTHON} -m flake8 ${S}/src
    # ${PYTHON} -m mypy ${S}/src

    bbnote "Building Python package ${PN} version ${PV}"
}

# do_install hook - install additional files
do_install:append() {
    # Install configuration files
    install -d ${D}${sysconfdir}/python-example
    if [ -f ${S}/config/example.conf ]; then
        install -m 0644 ${S}/config/example.conf ${D}${sysconfdir}/python-example/
    fi

    # Install systemd service for Python daemon (if applicable)
    # install -d ${D}${systemd_system_unitdir}
    # install -m 0644 ${WORKDIR}/python-example.service ${D}${systemd_system_unitdir}/

    # Install udev rules (if needed)
    # install -d ${D}${sysconfdir}/udev/rules.d
    # install -m 0644 ${WORKDIR}/99-python-example.rules ${D}${sysconfdir}/udev/rules.d/

    # Create runtime directories
    install -d ${D}${localstatedir}/lib/python-example
    install -d ${D}${localstatedir}/log/python-example
}

# Package organization
# Python packages are automatically split by setuptools3 class:
# ${PN}: Runtime package with .py and .pyc files
# ${PN}-dev: Development files (headers if any)
# ${PN}-dbg: Debug symbols
# ${PN}-staticdev: Static libraries (rare for Python)

FILES:${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/python_example \
    ${sysconfdir}/python-example \
    ${localstatedir}/lib/python-example \
    ${localstatedir}/log/python-example \
"

# Exclude source .py files if you want only .pyc (bytecode)
# FILES:${PN}:remove = "${PYTHON_SITEPACKAGES_DIR}/*/*.py"

# Include script files installed by setup.py console_scripts
FILES:${PN} += "${bindir}/python-example-cli"

# If package provides both Python 2 and 3 versions (not recommended):
# BBCLASSEXTEND = "native nativesdk"

# Example setup.py that works with this recipe:
#
# from setuptools import setup, find_packages
#
# setup(
#     name='python-example',
#     version='1.0',
#     author='Example Author',
#     author_email='author@example.com',
#     description='Example Python package for Yocto',
#     long_description=open('README.md').read(),
#     long_description_content_type='text/markdown',
#     url='https://github.com/example/python-example',
#     packages=find_packages(where='src'),
#     package_dir={'': 'src'},
#     classifiers=[
#         'Programming Language :: Python :: 3',
#         'License :: OSI Approved :: Apache Software License',
#         'Operating System :: POSIX :: Linux',
#     ],
#     python_requires='>=3.7',
#     install_requires=[
#         'requests>=2.25.0',
#         'numpy>=1.19.0',
#     ],
#     entry_points={
#         'console_scripts': [
#             'python-example-cli=python_example.cli:main',
#         ],
#     },
#     include_package_data=True,
#     package_data={
#         'python_example': ['data/*.json'],
#     },
# )

# Alternative: Using PEP 517/518 with pyproject.toml
# If package uses pyproject.toml instead of setup.py:
# inherit python_pep517
# DEPENDS += "python3-build-native python3-installer-native python3-wheel-native"

# Testing integration
# do_test() {
#     # Run pytest during build
#     ${PYTHON} -m pytest ${S}/tests -v
# }
# addtask test after do_compile before do_install

# Python bytecode compilation
# By default, Yocto compiles .py to .pyc during packaging
# Control with: PYTHON_PRECOMPILE = "1"  # or "0" to disable

# Common Python package debugging:
# bitbake python-example -c devpyshell  # Interactive Python shell
# bitbake python-example -c listtasks   # Show all available tasks
# bitbake-layers show-recipes "python3-*"  # Show available Python packages
