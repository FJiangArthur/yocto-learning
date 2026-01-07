# Recipe Development - Interview Questions

## Overview
This section covers advanced recipe development techniques suitable for Mid to Senior-level positions. These questions assess deep understanding of BitBake recipes, patching, version management, and complex build scenarios.

---

### Q1: How do you apply patches in a BitBake recipe? [Difficulty: Mid]

**Question:**
Explain the different methods for applying patches to source code in Yocto recipes. How does the patch ordering work, and what happens if a patch fails to apply?

**Expected Answer:**

**Method 1: Automatic Patch Application via SRC_URI**
```bitbake
SRC_URI = "https://example.com/myapp-${PV}.tar.gz \
           file://0001-fix-compilation-error.patch \
           file://0002-add-custom-feature.patch \
           file://0003-update-configuration.patch \
          "
```

**Patch Ordering:**
- Patches are applied in the order they appear in SRC_URI
- Use numeric prefixes (0001-, 0002-) to make order explicit
- Applied during `do_patch` task after `do_unpack`

**Patch Format:**
```bash
# Create patch from git
git format-patch -1 <commit-hash>

# Patch should have proper format:
From abc123... Mon Sep 17 00:00:00 2001
From: Developer Name <dev@example.com>
Date: Mon, 1 Jan 2024 10:00:00 +0000
Subject: [PATCH] Fix compilation error

Description of what the patch does.

Signed-off-by: Developer Name <dev@example.com>
---
 src/main.c | 5 +++--
 1 file changed, 3 insertions(+), 2 deletions(-)

diff --git a/src/main.c b/src/main.c
...
```

**Method 2: Manual Patching in do_patch**
```bitbake
do_patch:append() {
    cd ${S}
    patch -p1 < ${WORKDIR}/special.patch
}
```

**Method 3: Using quilt**
```bitbake
inherit patch
SRC_URI += "file://series"  # quilt series file
```

**Handling Patch Failures:**

When a patch fails:
```bash
ERROR: myapp-1.0-r0 do_patch: Command Error: 'quilt --quiltrc ...'
ERROR: Logfile of failure stored in: tmp/work/.../temp/log.do_patch.12345
```

**Debug and Fix:**
```bash
# Enter devshell to investigate
bitbake -c devshell myapp

# In devshell:
cd ${S}
quilt push  # Try applying patches manually
quilt refresh  # Update patch if needed

# Or regenerate patch from fixed code
git diff > new.patch
```

**Advanced Patch Configuration:**
```bitbake
# Apply patch to specific subdirectory
SRC_URI += "file://fix.patch;patchdir=src/submodule"

# Strip levels
SRC_URI += "file://fix.patch;striplevel=2"

# Conditional patches
SRC_URI += "file://arm-specific.patch;apply=${@bb.utils.contains('TARGET_ARCH', 'arm', 'yes', 'no', d)}"
```

**Key Points to Cover:**
- Patches must be in unified diff format
- Store patches in recipe directory or files/
- Use `devtool modify` to create patches more easily
- Patch context matters (striplevel)
- Failed patches stop the build

**Follow-up Questions:**
1. How would you create a patch using devtool?
2. What's the difference between .patch and .diff files in Yocto?

**Red Flags (Weak Answers):**
- Not knowing patches are automatically applied from SRC_URI
- Manually editing source code in ${S} directly
- Not understanding patch ordering
- Never used devshell for debugging

---

### Q2: Explain PACKAGECONFIG and its use cases [Difficulty: Mid]

**Question:**
What is PACKAGECONFIG and how does it provide flexible recipe configuration? Provide a real-world example.

**Expected Answer:**

**PACKAGECONFIG Overview:**
A mechanism to enable/disable recipe features at build time without modifying the recipe itself. It manages dependencies, configure flags, and build options based on feature selection.

**Syntax:**
```bitbake
PACKAGECONFIG ??= "feature1 feature2"

PACKAGECONFIG[feature1] = "--enable-feature1,--disable-feature1,dependency1,rdependency1,runtime-recommends,runtime-conflicts"
```

**Structure:**
```
PACKAGECONFIG[feature] = "enabled-flags, disabled-flags, build-deps, runtime-deps, runtime-recommends, runtime-conflicts"
```

**Real-World Example - OpenCV:**
```bitbake
DESCRIPTION = "Open Source Computer Vision Library"

# Default enabled features
PACKAGECONFIG ??= "jpeg png tiff"

# Optional features users can enable
PACKAGECONFIG[eigen] = "-DWITH_EIGEN=ON,-DWITH_EIGEN=OFF,eigen,"
PACKAGECONFIG[gstreamer] = "-DWITH_GSTREAMER=ON,-DWITH_GSTREAMER=OFF,gstreamer1.0 gstreamer1.0-plugins-base,gstreamer1.0-plugins-good"
PACKAGECONFIG[gtk] = "-DWITH_GTK=ON,-DWITH_GTK=OFF,gtk+3,"
PACKAGECONFIG[jpeg] = "-DWITH_JPEG=ON,-DWITH_JPEG=OFF,jpeg,"
PACKAGECONFIG[opencl] = "-DWITH_OPENCL=ON,-DWITH_OPENCL=OFF,opencl-headers,"
PACKAGECONFIG[png] = "-DWITH_PNG=ON,-DWITH_PNG=OFF,libpng,"
PACKAGECONFIG[python3] = "-DPYTHON3_EXECUTABLE=${PYTHON},-DWITH_PYTHON=OFF,python3-numpy-native,python3-numpy"
PACKAGECONFIG[tiff] = "-DWITH_TIFF=ON,-DWITH_TIFF=OFF,tiff,"

EXTRA_OECMAKE = "${@bb.utils.contains('PACKAGECONFIG', 'eigen', '-DEIGEN_INCLUDE_PATH=${STAGING_INCDIR}/eigen3', '', d)}"
```

**Usage in local.conf:**
```bitbake
# Add python3 support to opencv
PACKAGECONFIG:append:pn-opencv = " python3 gstreamer"

# Remove jpeg support
PACKAGECONFIG:remove:pn-opencv = "jpeg"
```

**Example - Custom Recipe:**
```bitbake
SUMMARY = "Network monitoring tool"
LICENSE = "MIT"

PACKAGECONFIG ??= "ssl ${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"

PACKAGECONFIG[ssl] = "--with-ssl,--without-ssl,openssl,openssl"
PACKAGECONFIG[systemd] = "--enable-systemd,--disable-systemd,systemd,systemd"
PACKAGECONFIG[gui] = "--enable-gui,--disable-gui,qt5,qt5"
PACKAGECONFIG[database] = "--with-database=postgres,--with-database=none,postgresql,postgresql"

inherit autotools

do_configure() {
    oe_runconf ${PACKAGECONFIG_CONFARGS}
}
```

**Advanced Usage:**
```bitbake
# Conditional defaults based on distro features
PACKAGECONFIG ??= "\
    ${@bb.utils.filter('DISTRO_FEATURES', 'systemd x11', d)} \
    ${@bb.utils.contains('TUNE_FEATURES', 'aarch64', 'neon', '', d)} \
"

# Machine-specific overrides
PACKAGECONFIG:jetson-nano = "cuda opencl gstreamer"
```

**Benefits:**
1. **Flexibility**: Users customize without editing recipes
2. **Dependency Management**: Automatically adds/removes dependencies
3. **Configure Flags**: Automatically adjusts build configuration
4. **Maintainability**: Centralized feature management

**Key Points to Cover:**
- PACKAGECONFIG is a space-separated list of features
- Each feature defines enabled/disabled behavior
- First field: flags when enabled
- Second field: flags when disabled
- Third field: build-time dependencies (DEPENDS)
- Fourth field: runtime dependencies (RDEPENDS)

**Follow-up Questions:**
1. How would you debug which PACKAGECONFIG flags are active for a recipe?
2. When would you use PACKAGECONFIG vs DISTRO_FEATURES?

**Red Flags (Weak Answers):**
- Not understanding the 6-field syntax
- Hardcoding features instead of using PACKAGECONFIG
- Not knowing PACKAGECONFIG can be overridden in local.conf
- Confusing with DISTRO_FEATURES

---

### Q3: How do you handle different source versions in a recipe? [Difficulty: Mid]

**Question:**
Explain how to manage multiple versions of the same software in Yocto. What is PREFERRED_VERSION and how does it interact with multiple recipe versions?

**Expected Answer:**

**Multiple Version Strategy:**

**File Organization:**
```
meta-layer/recipes-example/myapp/
├── myapp_1.0.bb
├── myapp_2.0.bb
├── myapp_git.bb
├── myapp.inc           # Common definitions
└── files/
    ├── common.patch
    └── version-specific/
        ├── 1.0/
        └── 2.0/
```

**Common Include File (myapp.inc):**
```bitbake
SUMMARY = "My Application"
HOMEPAGE = "https://example.com/myapp"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=abc123..."

DEPENDS = "openssl zlib"

inherit cmake

EXTRA_OECMAKE = "-DENABLE_TESTS=OFF"

do_install:append() {
    install -d ${D}${sysconfdir}/myapp
    install -m 0644 ${WORKDIR}/myapp.conf ${D}${sysconfdir}/myapp/
}
```

**Version-Specific Recipes:**

**myapp_1.0.bb:**
```bitbake
require myapp.inc

SRC_URI = "https://downloads.example.com/myapp-${PV}.tar.gz \
           file://0001-fix-v1-bug.patch \
          "
SRC_URI[sha256sum] = "abc123..."

# Version 1.0 specific configuration
EXTRA_OECMAKE += "-DLEGACY_MODE=ON"
```

**myapp_2.0.bb:**
```bitbake
require myapp.inc

SRC_URI = "https://downloads.example.com/myapp-${PV}.tar.gz \
           file://0001-enable-new-feature.patch \
          "
SRC_URI[sha256sum] = "def456..."

DEPENDS += "protobuf"  # New dependency in 2.0

# Version 2.0 specific
PACKAGECONFIG ??= "async-io"
PACKAGECONFIG[async-io] = "-DASYNC_IO=ON,-DASYNC_IO=OFF,libuv"
```

**myapp_git.bb (Development Version):**
```bitbake
require myapp.inc

SRCREV = "${AUTOREV}"
PV = "2.1+git${SRCPV}"

SRC_URI = "git://github.com/example/myapp.git;protocol=https;branch=develop"

S = "${WORKDIR}/git"

# Development features
EXTRA_OECMAKE += "-DENABLE_DEBUG=ON"
```

**PREFERRED_VERSION:**

**In local.conf or distro config:**
```bitbake
# Use specific version globally
PREFERRED_VERSION_myapp = "2.0"

# Use git version for development
PREFERRED_VERSION_myapp = "2.1+git%"

# Machine-specific version
PREFERRED_VERSION_myapp:jetson-nano = "2.0"
PREFERRED_VERSION_myapp:raspberrypi4 = "1.0"

# Version with operators
PREFERRED_VERSION_myapp = "2.%"  # Any 2.x version
```

**Default Version Selection:**
Without PREFERRED_VERSION, BitBake selects the highest version number:
```
myapp_1.0.bb
myapp_2.0.bb
myapp_2.1.bb  ← Selected by default
```

**Version Ranges in Dependencies:**
```bitbake
# Require minimum version
DEPENDS = "myapp (>= 2.0)"

# Specific version range
RDEPENDS:${PN} = "mylib (>= 1.5) mylib (<< 2.0)"
```

**Checking Active Version:**
```bash
# See which version will be built
bitbake -s | grep myapp

# Show version selection reasoning
bitbake -e myapp | grep "^PV="
bitbake -e myapp | grep "PREFERRED_VERSION"
```

**Recipe Upgrade Workflow:**
```bash
# Check for new versions
devtool check-upgrade-status myapp

# Upgrade recipe
devtool upgrade myapp --version 3.0

# Test new version
bitbake myapp

# Finalize upgrade
devtool finish myapp meta-custom
```

**PV (Package Version) Variable:**
```bitbake
# Auto-extracted from filename
# myapp_2.0.bb → PV = "2.0"

# Override if needed
PV = "2.0.1"

# Git-based versions
PV = "1.0+git${SRCPV}"  # Results in: 1.0+gitAUTOINC+abc123...
```

**Key Points to Cover:**
- Multiple recipe versions can coexist
- Use .inc files for common code
- PREFERRED_VERSION controls selection
- Higher versions are default without PREFERRED_VERSION
- Git recipes use special PV format with SRCPV
- Version-specific patches go in subdirectories

**Follow-up Questions:**
1. How would you maintain patches across multiple versions?
2. What happens if two recipes require different versions of the same dependency?

**Red Flags (Weak Answers):**
- Not knowing multiple versions can coexist
- Never used .inc files
- Not understanding PREFERRED_VERSION
- Duplicating code across version recipes

---

### Q4: Explain the difference between WORKDIR, S, B, and D [Difficulty: Mid]

**Question:**
Describe the purpose of the key directory variables WORKDIR, S, B, and D in BitBake recipes. When and how are they used?

**Expected Answer:**

**Key Directory Variables:**

**1. WORKDIR - Work Directory**
```bitbake
WORKDIR = "${TMPDIR}/work/${MULTIMACH_TARGET_SYS}/${PN}/${EXTENDPE}${PV}-${PR}"

# Example:
# tmp/work/cortexa57-poky-linux/myapp/1.0-r0/
```

Contents:
```
WORKDIR/
├── myapp-1.0/          # Unpacked source (this is ${S})
├── build/              # Build directory (this is ${B})
├── image/              # Install staging (this is ${D})
├── temp/               # Task logs and scripts
├── package/            # Split packages
├── packages-split/     # Per-package files
├── deploy-*/           # Package outputs
└── *.patch             # Patches from SRC_URI
```

**2. S - Source Directory**
```bitbake
S = "${WORKDIR}/${BP}"  # BP = ${BPN}-${PV}

# For tarball: myapp-1.0.tar.gz
# S = ${WORKDIR}/myapp-1.0/

# For git with non-standard directory:
S = "${WORKDIR}/git"

# For multiple source trees:
S = "${WORKDIR}/custom-source-dir"
```

Usage:
```bitbake
do_configure() {
    cd ${S}  # Enter source directory
    ./configure --prefix=/usr
}
```

**3. B - Build Directory**
```bitbake
# Default: same as source
B = "${S}"

# Out-of-tree builds (recommended):
B = "${WORKDIR}/build"
```

Example with CMake:
```bitbake
inherit cmake

# cmake.bbclass sets:
B = "${WORKDIR}/build"

do_configure() {
    cd ${B}
    cmake ${S}  # Configure from source, build in ${B}
}

do_compile() {
    cd ${B}
    oe_runmake  # Build in build directory
}
```

**4. D - Destination/Install Directory**
```bitbake
D = "${WORKDIR}/image"

# This represents the root filesystem
# Files installed to ${D}/usr/bin appear as /usr/bin on target
```

Usage in do_install:
```bitbake
do_install() {
    # Install binary
    install -d ${D}${bindir}
    install -m 0755 ${B}/myapp ${D}${bindir}/

    # Install library
    install -d ${D}${libdir}
    install -m 0755 ${B}/libmyapp.so.1.0 ${D}${libdir}/
    ln -sf libmyapp.so.1.0 ${D}${libdir}/libmyapp.so.1
    ln -sf libmyapp.so.1 ${D}${libdir}/libmyapp.so

    # Install config
    install -d ${D}${sysconfdir}/myapp
    install -m 0644 ${WORKDIR}/myapp.conf ${D}${sysconfdir}/myapp/

    # Install headers (will go to -dev package)
    install -d ${D}${includedir}/myapp
    install -m 0644 ${S}/include/*.h ${D}${includedir}/myapp/
}
```

**Complete Example:**

```bitbake
SUMMARY = "Example application"
LICENSE = "MIT"

SRC_URI = "https://example.com/myapp-${PV}.tar.gz \
           file://build-fix.patch \
           file://myapp.conf \
          "

# Source unpacked to ${WORKDIR}/myapp-1.0/
S = "${WORKDIR}/myapp-${PV}"

# Build in separate directory
B = "${WORKDIR}/build"

inherit cmake

do_configure() {
    # Work in build directory
    cd ${B}
    # Reference source directory
    cmake ${S} -DCMAKE_INSTALL_PREFIX=/usr
}

do_compile() {
    # Compile in build directory
    cd ${B}
    oe_runmake
}

do_install() {
    # Install from build directory to staging root
    cd ${B}
    oe_runmake install DESTDIR=${D}

    # Manual installations to staging root
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/myapp.conf ${D}${sysconfdir}/
}
```

**Directory Lifecycle:**

```
do_fetch → downloads/myapp-1.0.tar.gz

do_unpack → ${WORKDIR}/myapp-1.0/  (this is ${S})

do_patch → Patches applied to ${S}

do_configure → Usually in ${B}, reads ${S}

do_compile → Builds in ${B}

do_install → Installs from ${B} to ${D}

do_package → Reads ${D}, creates packages
```

**Common Patterns:**

```bitbake
# Pattern 1: In-tree build (autotools default)
S = "${WORKDIR}/${BP}"
B = "${S}"

# Pattern 2: Out-of-tree build (cmake, meson)
S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

# Pattern 3: Custom source structure
S = "${WORKDIR}/custom-name"  # Override default

# Pattern 4: Subdirectory build
S = "${WORKDIR}/${BP}/src"
```

**Debugging:**
```bash
# Enter recipe work environment
bitbake -c devshell myapp

# In devshell, examine:
echo $WORKDIR  # /path/to/tmp/work/...
echo $S        # Source location
echo $B        # Build location
echo $D        # Install staging

# View variables
bitbake -e myapp | grep "^S="
```

**Key Points to Cover:**
- WORKDIR is the container for all recipe work
- S is where source code lives
- B is where compilation happens (can be separate)
- D is the staging area for installation
- Never hardcode these paths
- Out-of-tree builds keep source clean

**Follow-up Questions:**
1. Why would you use a separate build directory (B != S)?
2. What happens if you install files directly to / instead of ${D}/?

**Red Flags (Weak Answers):**
- Confusing S and B
- Not knowing D is a staging area
- Hardcoding absolute paths
- Not understanding out-of-tree builds
- Installing to system directories instead of ${D}

---

### Q5: How do you create a recipe from scratch using devtool? [Difficulty: Mid]

**Question:**
Demonstrate how to use devtool to create a new recipe, make modifications, and finalize it into a layer. What are the advantages of this workflow?

**Expected Answer:**

**devtool Workflow Overview:**

devtool is a development tool that simplifies recipe creation, modification, and testing by automating common tasks and maintaining a workspace.

**Creating a Recipe from Scratch:**

**1. Create Recipe from Git Repository:**
```bash
# Create recipe for a git project
devtool add myapp https://github.com/example/myapp.git

# Specify branch
devtool add myapp https://github.com/example/myapp.git --srcbranch develop

# Specify version
devtool add myapp https://github.com/example/myapp.git --version 1.2.3
```

**What devtool does:**
- Creates workspace in `workspace/recipes/myapp/`
- Generates initial recipe with detected build system
- Clones source to `workspace/sources/myapp/`
- Makes recipe immediately buildable

**2. Create Recipe from Tarball:**
```bash
devtool add myapp https://example.com/releases/myapp-1.0.tar.gz
```

**3. Create Recipe from Local Source:**
```bash
# Extract to workspace and create recipe
devtool add myapp /path/to/myapp-source/

# Or create empty recipe
devtool add myapp --no-same-dir
```

**Generated Recipe Example:**
```bitbake
# workspace/recipes/myapp/myapp_git.bb
SUMMARY = "Recipe created by devtool"
LICENSE = "CLOSED"  # TODO: Set license

SRC_URI = "git://github.com/example/myapp.git;protocol=https;branch=master"
SRCREV = "abc123def456..."

S = "${WORKDIR}/git"

inherit cmake  # Auto-detected

# Manual configuration may be needed
DEPENDS = "openssl zlib"
```

**Modifying Source Code:**

**4. Edit Source in Workspace:**
```bash
# Source is at: workspace/sources/myapp/
cd workspace/sources/myapp/

# Make changes
vim src/main.c
git add src/main.c
git commit -m "Fix bug in main function"
```

**5. Test Build:**
```bash
# Build with modifications
bitbake myapp

# Run do_install only
bitbake -c install myapp

# Deploy to target
devtool deploy-target myapp root@192.168.1.100
```

**6. Update Recipe:**
```bash
# Update SRCREV to latest commit
devtool update-recipe myapp

# Update recipe with all commits as patches
devtool update-recipe myapp --append workspace-layer

# Create patches for specific commits
devtool update-recipe myapp --initial-rev abc123
```

**Finalizing Recipe:**

**7. Extract to Layer:**
```bash
# Move recipe to proper layer
devtool finish myapp meta-custom

# Specify recipe directory
devtool finish myapp meta-custom/recipes-apps/myapp
```

**Result:**
```
meta-custom/recipes-apps/myapp/
├── myapp_git.bb
└── files/
    └── 0001-Fix-bug-in-main-function.patch
```

**Complete Workflow Example:**

```bash
# 1. Create recipe from git
devtool add opencv https://github.com/opencv/opencv.git --version 4.6.0

# 2. Examine generated recipe
cat workspace/recipes/opencv/opencv_git.bb

# 3. Edit recipe to add dependencies
devtool edit-recipe opencv
# Add: DEPENDS = "python3 numpy jpeg libpng"

# 4. Build and test
bitbake opencv

# 5. Modify source code
cd workspace/sources/opencv
git checkout -b custom-changes
vim modules/core/src/system.cpp
git add .
git commit -m "Add custom feature"

# 6. Rebuild with changes
bitbake -c compile opencv -f
bitbake opencv

# 7. Update recipe with patches
devtool update-recipe opencv

# 8. Deploy to target for testing
devtool deploy-target opencv root@jetson-nano

# 9. Finalize to layer
devtool finish opencv meta-custom
```

**Advanced devtool Commands:**

```bash
# Modify existing recipe
devtool modify opencv

# Check upgrade status
devtool check-upgrade-status opencv

# Upgrade to new version
devtool upgrade opencv --version 4.7.0

# Reset workspace
devtool reset opencv

# Show workspace status
devtool status

# Build SDK for external development
devtool build-sdk

# Search for recipes
devtool search python
```

**devtool Configuration:**
```bitbake
# conf/local.conf
# Workspace location
DEVTOOL_WORKSPACE = "${TOPDIR}/workspace"

# Auto-detect license
DEVTOOL_DISABLE_LICENSE_CHECK = "1"
```

**Advantages of devtool Workflow:**

1. **Automated Recipe Creation**: Detects build system, generates proper syntax
2. **Integrated Source Management**: Git integration for patches
3. **Iterative Development**: Modify-build-test cycle without recipe editing
4. **Automatic Patch Generation**: Converts git commits to patches
5. **Easy Deployment**: Test on target without image rebuild
6. **Version Upgrades**: Simplified upgrade workflow
7. **Workspace Isolation**: Doesn't affect main layers during development

**devtool vs Manual Recipe Creation:**

| Aspect | devtool | Manual |
|--------|---------|--------|
| Initial setup | Automatic | Manual typing |
| Build system detection | Yes | Manual |
| Source management | Git workspace | Manual patching |
| Testing | Incremental | Full rebuild |
| Patch creation | Automatic | git format-patch |
| Learning curve | Lower | Higher |

**Key Points to Cover:**
- devtool manages a workspace directory
- Automatically detects build systems (CMake, autotools, etc.)
- Source modifications tracked via git
- update-recipe converts commits to patches
- finish command integrates into layer
- Supports iterative development workflow

**Follow-up Questions:**
1. How would you use devtool to upgrade an existing recipe to a new version?
2. What's the difference between `devtool add` and `devtool modify`?

**Red Flags (Weak Answers):**
- Never used devtool
- Manually creating all recipes without automation
- Not understanding workspace concept
- Not knowing about deploy-target for testing

---

### Q6: Explain how to debug a failed recipe build [Difficulty: Senior]

**Question:**
Walk through your systematic approach to debugging a recipe build failure. What tools and techniques do you use?

**Expected Answer:**

**Systematic Debugging Approach:**

**1. Read the Error Message:**

```bash
ERROR: myapp-1.0-r0 do_compile: ExecutionError: ...
ERROR: Logfile of failure stored in: /path/to/tmp/work/.../temp/log.do_compile.12345
```

**First Steps:**
- Identify which task failed (do_fetch, do_compile, do_install)
- Read the log file completely
- Check the last 50 lines for actual error

**2. Examine Log Files:**

```bash
# Primary log location
tmp/work/architecture/recipe-name/version/temp/

# Key logs:
log.do_compile.12345      # Task output
run.do_compile.12345      # Actual shell script that ran
log.task_order            # Task execution order
```

**Read the log:**
```bash
# View last 100 lines
tail -100 tmp/work/cortexa57-poky-linux/myapp/1.0-r0/temp/log.do_compile.12345

# Search for errors
grep -i error tmp/work/.../temp/log.do_compile.12345
grep -i "undefined reference" tmp/work/.../temp/log.do_compile.12345
```

**3. Use devshell for Interactive Debugging:**

```bash
# Enter recipe environment
bitbake -c devshell myapp
```

In devshell:
```bash
# Environment is pre-configured
echo $S          # Source directory
echo $B          # Build directory
echo $CC         # Compiler
echo $CFLAGS     # Compiler flags

# Try manual build
cd $S
./configure --prefix=/usr
make

# Or for CMake:
cd $B
cmake $S
make VERBOSE=1
```

**4. Common Failure Patterns:**

**A. Missing Dependencies:**
```
error: openssl/ssl.h: No such file or directory
```

**Fix:**
```bitbake
DEPENDS += "openssl"
```

**B. Wrong Paths:**
```
error: cannot find -lssl
```

**Debug:**
```bash
bitbake -c devshell myapp
echo $LDFLAGS  # Check library paths
ls ${STAGING_LIBDIR}  # Verify libraries are staged
```

**Fix:**
```bitbake
LDFLAGS += "-L${STAGING_LIBDIR}"
# Or ensure dependency in DEPENDS
```

**C. Cross-compilation Issues:**
```
error: cannot execute binary: Exec format error
```

**Problem:** Trying to run target binary on build host

**Fix:**
```bitbake
do_compile() {
    # Don't run target binaries during build
    # Use native tools or QEMU if needed
    oe_runmake CROSS_COMPILE=${TARGET_PREFIX}
}
```

**D. Parallel Build Failures:**
```
error: file not found (intermittent)
```

**Fix:**
```bitbake
PARALLEL_MAKE = "-j 1"  # Disable parallel build
# Or fix Makefile dependencies
```

**5. Environment Variable Inspection:**

```bash
# Dump all variables for recipe
bitbake -e myapp > myapp-env.txt

# Search for specific variable
bitbake -e myapp | grep "^DEPENDS="
bitbake -e myapp | grep "^CFLAGS="

# Check override application
bitbake -e myapp | grep -A 5 "^SRC_URI="
```

**6. Task Re-execution:**

```bash
# Clean specific task
bitbake -c cleansstate myapp
bitbake -c compile myapp

# Force re-run without cleaning
bitbake -c compile -f myapp

# Clean everything
bitbake -c cleanall myapp
```

**7. Check Source Code:**

```bash
# Navigate to source
cd tmp/work/cortexa57-poky-linux/myapp/1.0-r0/myapp-1.0/

# Check if patches applied correctly
cat .pc/applied-patches

# Look at modified files
git diff
```

**8. Build System Specific Debugging:**

**CMake:**
```bitbake
do_compile() {
    cd ${B}
    cmake ${S} -LAH  # List all variables
    make VERBOSE=1   # Verbose output
}
```

**Autotools:**
```bitbake
do_configure() {
    ./configure --help  # See all options
    ./configure --prefix=/usr CFLAGS="-g -O0"  # Debug flags
}
```

**9. Advanced Debugging Techniques:**

**A. Enable Debug Output:**
```bitbake
# In recipe
DEBUG_BUILD = "1"
EXTRA_OEMAKE += "V=1"  # Verbose make

# In local.conf
BB_NUMBER_THREADS = "1"  # Single-threaded BitBake
PARALLEL_MAKE = "-j 1"   # Single-threaded make
```

**B. Preserve Work Directory:**
```bitbake
# In local.conf
RM_WORK_EXCLUDE += "myapp"  # Don't delete after build
```

**C. Use strace:**
```bash
bitbake -c devshell myapp
strace -f -e trace=open,stat ./configure
```

**10. Real-World Example:**

**Problem:**
```
ERROR: myapp-1.0-r0 do_compile: oe_runmake failed
ERROR: Logfile of failure: tmp/work/.../log.do_compile.1234
```

**Investigation:**
```bash
# Step 1: Read log
tail -100 tmp/work/.../log.do_compile.1234
# Shows: "fatal error: Python.h: No such file or directory"

# Step 2: Check recipe
cat recipes-apps/myapp/myapp_1.0.bb
# Missing python3 in DEPENDS

# Step 3: Check what's needed
bitbake -c devshell myapp
pkg-config --cflags python3  # Not found

# Step 4: Fix recipe
```

**Fix:**
```bitbake
DEPENDS += "python3"

# If python3 headers needed:
inherit python3native
# or
DEPENDS += "python3-native"
```

**11. Debugging Checklist:**

```markdown
- [ ] Read complete error log
- [ ] Identify failed task
- [ ] Check task script (run.do_taskname)
- [ ] Verify dependencies in DEPENDS
- [ ] Check file paths and variables (bitbake -e)
- [ ] Test in devshell
- [ ] Verify patches applied correctly
- [ ] Check for cross-compilation issues
- [ ] Try disabling parallel build
- [ ] Search for similar issues in Yocto mailing list
- [ ] Check recipe syntax with bitbake-getvar
```

**Key Tools:**
- `bitbake -c devshell` - Interactive environment
- `bitbake -e` - Variable expansion
- `bitbake -c listtasks` - Available tasks
- `bitbake -g` - Dependency graph
- `devtool modify` - Recipe modification workflow
- Log files in tmp/work/.../temp/

**Key Points to Cover:**
- Always read complete log files
- Use devshell for interactive testing
- Understand cross-compilation environment
- Check dependencies and paths
- Know common failure patterns
- Use systematic approach, not random changes

**Follow-up Questions:**
1. How would you debug a recipe that builds successfully locally but fails in CI?
2. What would you do if a recipe fails intermittently?

**Red Flags (Weak Answers):**
- Not reading log files thoroughly
- Making random changes hoping it works
- Not using devshell
- Not understanding cross-compilation errors
- Giving up too quickly without systematic approach

---

### Q7: Explain OVERRIDES and override syntax [Difficulty: Senior]

**Question:**
Describe how the override mechanism works in BitBake. Explain the syntax and provide examples of common override patterns.

**Expected Answer:**

**Override Mechanism:**

Overrides allow conditional variable values based on machine, architecture, distro, or custom flags. BitBake evaluates overrides at the end of recipe parsing.

**Basic Syntax:**

**Old Style (Yocto 3.3 and earlier):**
```bitbake
VARIABLE_override = "value"
```

**New Style (Yocto 3.4+, Honister and later):**
```bitbake
VARIABLE:override = "value"
```

**OVERRIDES Variable:**
```bitbake
# Built automatically from:
OVERRIDES = "architecture:machine:distro:libc:class:forcevariable:..."

# Example:
OVERRIDES = "arm:armv7:jetson-nano:poky:glibc:class-target"
```

**Common Override Patterns:**

**1. Machine-Specific Overrides:**
```bitbake
# Default value
KERNEL_DEVICETREE = "generic.dtb"

# Machine-specific override
KERNEL_DEVICETREE:jetson-nano = "tegra210-p3450-0000.dtb"
KERNEL_DEVICETREE:raspberrypi4 = "bcm2711-rpi-4-b.dtb"
KERNEL_DEVICETREE:qemuarm64 = "qemu-arm64.dtb"
```

**2. Architecture Overrides:**
```bitbake
PACKAGECONFIG = "default-features"
PACKAGECONFIG:arm = "neon"
PACKAGECONFIG:x86 = "sse4"
PACKAGECONFIG:aarch64 = "neon fp16"

CFLAGS:append:arm = " -mfpu=neon"
CFLAGS:append:x86-64 = " -msse4.2"
```

**3. Distro Overrides:**
```bitbake
IMAGE_INSTALL = "base-packages"
IMAGE_INSTALL:append:poky = " poky-specific-tools"
IMAGE_INSTALL:append:mydistro = " custom-packages"
```

**4. Class Overrides:**
```bitbake
# Different for native vs target
DEPENDS = "openssl zlib"
DEPENDS:class-native = "openssl-native zlib-native"

# Cross compilation tools
EXTRA_OEMAKE:class-target = "CROSS_COMPILE=${TARGET_PREFIX}"
EXTRA_OEMAKE:class-native = ""
```

**5. Append/Prepend/Remove Operations:**

```bitbake
# Base value
IMAGE_FEATURES = "ssh-server-openssh"

# Append with space (always applied)
IMAGE_FEATURES:append = " debug-tweaks"

# Conditional append
IMAGE_FEATURES:append:jetson-nano = " cuda"

# Prepend
IMAGE_FEATURES:prepend = "read-only-rootfs "

# Remove
IMAGE_FEATURES:remove = "ssh-server-openssh"
IMAGE_FEATURES:remove:production = "debug-tweaks"
```

**6. Package-Specific Overrides:**
```bitbake
# Using recipe name
RDEPENDS:${PN} = "bash"
RDEPENDS:${PN}:jetson-nano = "bash cuda-runtime"

# Sub-packages
RDEPENDS:${PN}-dev = "development-libs"
RDEPENDS:${PN}-tools = "python3"
```

**Override Order and Priority:**

**Override Priority (right-most wins):**
```bitbake
OVERRIDES = "arm:armv7a:jetson-nano:poky"

VARIABLE = "default"
VARIABLE:arm = "arm-value"        # Overridden by next
VARIABLE:jetson-nano = "jetson"   # This wins!
```

**Multiple Overrides:**
```bitbake
# Combine overrides with :
KERNEL_ARGS:jetson-nano:production = "quiet splash"

# Applied only when both jetson-nano AND production are in OVERRIDES
```

**Advanced Override Patterns:**

**7. libc Overrides:**
```bitbake
DEPENDS = "base-library"
DEPENDS:libc-musl = "musl-specific-lib"
DEPENDS:libc-glibc = "glibc-specific-lib"
```

**8. Virtual Class Overrides:**
```bitbake
# Recipe: myapp.bb
BBCLASSEXTEND = "native nativesdk"

# Applied when building myapp-native
DEPENDS:class-native = "tool-native"

# Applied when building nativesdk-myapp
DEPENDS:class-nativesdk = "nativesdk-tool"
```

**9. Custom Overrides:**
```bitbake
# Define custom override
OVERRIDES:append = ":production"

# Use in recipes
IMAGE_FEATURES:production = ""  # No debug features
ROOT_PASSWORD:production = "secure-hash"
```

**Real-World Example - Jetson Platform:**

```bitbake
DESCRIPTION = "CUDA-accelerated application"

# Base dependencies
DEPENDS = "opencv"

# Jetson-specific dependencies
DEPENDS:append:tegra = " cuda-toolkit cudnn tensorrt"

# Source varies by platform
SRC_URI = "https://example.com/myapp-generic.tar.gz"
SRC_URI:jetson-nano = "https://example.com/myapp-jetson.tar.gz"

# Compilation flags
EXTRA_OEMAKE = ""
EXTRA_OEMAKE:tegra = "CUDA_ENABLED=1 CUDA_PATH=${STAGING_DIR_HOST}/usr/local/cuda"

# Package configuration
PACKAGECONFIG = "cpu-only"
PACKAGECONFIG:tegra = "cuda tensorrt"

PACKAGECONFIG[cuda] = "--enable-cuda,--disable-cuda,cuda-toolkit"
PACKAGECONFIG[tensorrt] = "--enable-tensorrt,--disable-tensorrt,tensorrt"

# Install differs by platform
do_install:append:tegra() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/myapp-cuda ${D}${bindir}/
}

# Runtime dependencies
RDEPENDS:${PN}:tegra = "cuda-runtime libcudnn libnvinfer"
```

**Override Evaluation:**

```bitbake
# How BitBake evaluates:

# 1. Parse base value
VARIABLE = "base"

# 2. Apply override if in OVERRIDES
VARIABLE:arm = "arm-value"

# 3. Apply append/prepend/remove operations
VARIABLE:append = " extra"
VARIABLE:append:arm = " arm-extra"

# 4. Final result (if OVERRIDES contains "arm"):
VARIABLE = "arm-value extra arm-extra"
```

**Debugging Overrides:**

```bash
# Check OVERRIDES value
bitbake -e myapp | grep "^OVERRIDES="

# See variable after override application
bitbake -e myapp | grep "^DEPENDS="

# Trace override application
bitbake -e myapp | grep -A 20 "^# DEPENDS="
```

**Migration from Old to New Syntax:**

```bitbake
# Old (Yocto 3.3 and before)
SRC_URI_append_jetson-nano = " file://jetson-patch.patch"
DEPENDS_append_class-native = " tool-native"

# New (Yocto 3.4+)
SRC_URI:append:jetson-nano = " file://jetson-patch.patch"
DEPENDS:append:class-native = " tool-native"

# Conversion script available:
# scripts/contrib/convert-overrides.py
```

**Key Points to Cover:**
- Overrides are evaluated at the end of parsing
- Use colon (:) syntax in modern Yocto
- Right-most override wins
- append/prepend/remove are special operations
- Custom overrides can be added to OVERRIDES
- Machine, architecture, distro are common overrides

**Follow-up Questions:**
1. What happens if multiple overrides set the same variable?
2. How would you add a custom override for a product variant?

**Red Flags (Weak Answers):**
- Using old underscore syntax in new recipes
- Not understanding override priority
- Confusing append (always applies) with :append (conditional)
- Not knowing OVERRIDES determines which override applies

---

### Q8: How do you handle LICENSE and LIC_FILES_CHKSUM? [Difficulty: Mid]

**Question:**
Explain the importance of license management in Yocto recipes. How do you properly set LICENSE and LIC_FILES_CHKSUM?

**Expected Answer:**

**License Management in Yocto:**

Yocto enforces license tracking to ensure compliance with open-source and commercial license requirements.

**Basic License Declaration:**

```bitbake
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=abc123def456..."
```

**Common Licenses:**

```bitbake
# Single license
LICENSE = "MIT"
LICENSE = "GPLv2"
LICENSE = "Apache-2.0"
LICENSE = "BSD-3-Clause"
LICENSE = "CLOSED"  # Proprietary, no source distribution

# Multiple licenses (AND)
LICENSE = "GPLv2 & LGPLv2.1"

# Multiple licenses (OR)
LICENSE = "GPLv2 | MIT"

# Complex combinations
LICENSE = "(GPLv2 | MIT) & BSD-3-Clause"
```

**LIC_FILES_CHKSUM:**

Specifies files containing license text and their MD5 checksums to detect license changes.

**Syntax:**
```bitbake
LIC_FILES_CHKSUM = "file://path/to/license;md5=checksum"

# Multiple license files
LIC_FILES_CHKSUM = "file://COPYING;md5=abc123... \
                    file://LICENSE.MIT;md5=def456... \
                   "

# With begin/end line numbers
LIC_FILES_CHKSUM = "file://main.c;beginline=1;endline=20;md5=abc123..."
```

**Finding License Files:**

```bash
# Extract source to examine
bitbake -c unpack myapp
cd tmp/work/.../myapp-1.0/

# Common license file names:
ls LICENSE COPYING COPYRIGHT LICENCE NOTICE

# Check source file headers
head -20 src/main.c
```

**Generating MD5 Checksum:**

```bash
# After extracting source
cd tmp/work/.../myapp-1.0/
md5sum LICENSE

# Output: abc123def456... LICENSE

# Use in recipe:
LIC_FILES_CHKSUM = "file://LICENSE;md5=abc123def456..."
```

**Complete Example:**

```bitbake
SUMMARY = "Example application"
HOMEPAGE = "https://example.com/myapp"
LICENSE = "MIT"

# License file at root of source tree
LIC_FILES_CHKSUM = "file://LICENSE;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://example.com/myapp-${PV}.tar.gz"

inherit cmake
```

**Multi-License Example:**

```bitbake
SUMMARY = "Complex licensing example"

# Application is MIT, but uses GPLv2 library
LICENSE = "MIT & GPLv2"

LIC_FILES_CHKSUM = "file://LICENSE;md5=abc123... \
                    file://lib/gpl-library/COPYING;md5=def456... \
                   "

# Track which package has which license
LICENSE:${PN} = "MIT"
LICENSE:${PN}-gpl-plugin = "GPLv2"
```

**License from Source File Header:**

```bitbake
# When no separate LICENSE file exists
LIC_FILES_CHKSUM = "file://src/main.c;beginline=1;endline=16;md5=abc123..."

# main.c contains:
# /*
#  * Copyright (C) 2024 Example Corp
#  * Licensed under MIT License
#  * ...
#  */
```

**Handling License Changes:**

**Scenario:** Upgrade recipe, license file changed

```bash
bitbake myapp
ERROR: myapp-2.0-r0 do_populate_lic: LIC_FILES_CHKSUM mismatch
ERROR: Expected md5sum: abc123
ERROR: Actual md5sum: def456
```

**Resolution:**
```bash
# Review license changes
cd tmp/work/.../myapp-2.0/
diff ../myapp-1.0/LICENSE LICENSE

# If acceptable, update checksum
md5sum LICENSE
# Update LIC_FILES_CHKSUM in recipe
```

**CLOSED License (Proprietary):**

```bitbake
SUMMARY = "Proprietary application"
LICENSE = "CLOSED"

# No LIC_FILES_CHKSUM required for CLOSED
# But document commercial license

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/proprietary-binary ${D}${bindir}/
}
```

**Commercial Licenses:**

```bitbake
# In local.conf - accept commercial licenses
LICENSE_FLAGS_ACCEPTED = "commercial"

# Recipe with commercial license
LICENSE = "Commercial"
LICENSE_FLAGS = "commercial"

LIC_FILES_CHKSUM = "file://EULA.txt;md5=abc123..."
```

**License Exclusion:**

```bitbake
# In local.conf - exclude GPL3 packages
INCOMPATIBLE_LICENSE = "GPLv3 GPL-3.0"

# In distro config
INCOMPATIBLE_LICENSE = "GPL-3.0-only GPL-3.0-or-later"
```

**License Auditing:**

```bash
# Generate license manifest
bitbake core-image-minimal

# License information in:
tmp/deploy/licenses/core-image-minimal/
tmp/deploy/licenses/core-image-minimal/license.manifest

# View package licenses
cat tmp/deploy/licenses/core-image-minimal/license.manifest
```

**Custom License:**

```bitbake
# For uncommon licenses
LICENSE = "MyCustomLicense"

# Store license text in layer
# meta-custom/files/custom-licenses/MyCustomLicense
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MyCustomLicense;md5=..."

# Or from custom location
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-custom/files/licenses/MyCustomLicense;md5=..."
```

**Best Practices:**

```bitbake
# 1. Always specify exact license
LICENSE = "Apache-2.0"  # Good
LICENSE = "Apache"      # Too vague

# 2. Use SPDX identifiers
LICENSE = "GPL-2.0-only"       # Good (SPDX)
LICENSE = "GPLv2"              # Old style

# 3. Document multi-licensing clearly
LICENSE = "MIT & GPLv2"
# With comment explaining why

# 4. Track all license files
LIC_FILES_CHKSUM = "file://LICENSE;md5=... \
                    file://NOTICE;md5=... \
                   "

# 5. Review license changes on upgrades
# Don't blindly update checksums
```

**Real-World Example - CUDA:**

```bitbake
SUMMARY = "NVIDIA CUDA Toolkit"
LICENSE = "NVIDIA-CUDA"
LIC_FILES_CHKSUM = "file://EULA.txt;md5=2c2c0f7e19bb6661c0c66bb799be8b82"

# Commercial license flag
LICENSE_FLAGS = "commercial_nvidia-cuda"

# User must accept in local.conf:
# LICENSE_FLAGS_ACCEPTED = "commercial_nvidia-cuda"
```

**Key Points to Cover:**
- LICENSE is mandatory (except for image recipes)
- LIC_FILES_CHKSUM ensures license changes are noticed
- MD5 checksum detects license modifications
- Support multiple licenses with & and |
- CLOSED for proprietary software
- License compliance is auditable
- SPDX identifiers are preferred

**Follow-up Questions:**
1. What happens if LIC_FILES_CHKSUM doesn't match?
2. How would you exclude all GPL3 packages from an image?

**Red Flags (Weak Answers):**
- Using LICENSE = "CLOSED" without understanding implications
- Not knowing how to generate MD5 checksums
- Ignoring license changes during upgrades
- Not understanding AND vs OR in license combinations
- Never auditing final image licenses

---

### Q9: Explain the role of classes (inherit) in recipes [Difficulty: Mid]

**Question:**
What are BitBake classes and how do you use them in recipes? Describe common classes and when to inherit them.

**Expected Answer:**

**BitBake Classes:**

Classes are reusable components that provide common functionality to recipes. They define standard tasks, variables, and functions that recipes can inherit.

**Class Location:**
```
meta-layer/classes/
meta-layer/classes-recipe/    # Recipe-specific classes (Yocto 4.0+)
meta-layer/classes-global/    # Global classes (Yocto 4.0+)
```

**Basic Inheritance:**

```bitbake
inherit class-name

# Multiple classes
inherit cmake pkgconfig systemd

# Conditional inheritance
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}
```

**Common Classes:**

**1. autotools (Autoconf/Automake projects):**
```bitbake
inherit autotools

# Provides:
# - do_configure: runs ./configure
# - do_compile: runs make
# - do_install: runs make install

# Automatically uses:
# - ${S}/configure
# - EXTRA_OECONF for configure args
# - EXTRA_OEMAKE for make args

EXTRA_OECONF = "--enable-feature --with-ssl"
```

**2. cmake (CMake projects):**
```bitbake
inherit cmake

# Provides:
# - Out-of-tree build (B != S)
# - do_configure: runs cmake
# - do_compile: runs make

EXTRA_OECMAKE = "-DENABLE_TESTS=OFF -DBUILD_SHARED_LIBS=ON"

# CMake-specific variables:
CMAKE_C_COMPILER = "${CC}"
CMAKE_CXX_COMPILER = "${CXX}"
```

**3. systemd (systemd service integration):**
```bitbake
inherit systemd

SYSTEMD_SERVICE:${PN} = "myapp.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/myapp.service ${D}${systemd_system_unitdir}/
}

FILES:${PN} += "${systemd_system_unitdir}/myapp.service"
```

**4. update-rc.d (SysV init scripts):**
```bitbake
inherit update-rc.d

INITSCRIPT_NAME = "myapp"
INITSCRIPT_PARAMS = "defaults 99"

do_install:append() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/myapp.init ${D}${sysconfdir}/init.d/myapp
}
```

**5. pkgconfig (pkg-config support):**
```bitbake
inherit pkgconfig

# Provides:
# - PKG_CONFIG_PATH setup
# - Dependency on pkgconfig-native

# Use in recipes that need pkg-config
do_configure:prepend() {
    export PKG_CONFIG_PATH="${PKG_CONFIG_PATH}"
}
```

**6. python3-dir (Python 3 recipes):**
```bitbake
inherit setuptools3

# For Python packages using setuptools
# Provides:
# - do_compile: python3 setup.py build
# - do_install: python3 setup.py install

RDEPENDS:${PN} = "python3-core"
```

**7. kernel (Linux kernel recipes):**
```bitbake
inherit kernel

# Provides:
# - Kernel-specific build tasks
# - do_configure: kernel configuration
# - do_compile: kernel and modules build
# - Module installation

KERNEL_DEVICETREE = "tegra210-p3450-0000.dtb"
KERNEL_IMAGETYPE = "Image"
```

**8. native (Build tools):**
```bitbake
inherit native

# Transforms recipe for build host
# - Runs on build machine
# - Installed to native sysroot

DEPENDS = "zlib-native"
```

**9. update-alternatives (Alternative implementations):**
```bitbake
inherit update-alternatives

ALTERNATIVE:${PN} = "editor"
ALTERNATIVE_TARGET[editor] = "${bindir}/vim"
ALTERNATIVE_LINK_NAME[editor] = "${bindir}/editor"
ALTERNATIVE_PRIORITY[editor] = "100"
```

**10. useradd (User/group creation):**
```bitbake
inherit useradd

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-u 1200 -d /home/myapp -r -s /bin/bash myapp"
GROUPADD_PARAM:${PN} = "-g 1200 myapp"
```

**Custom Class Example:**

**meta-custom/classes/custom-deploy.bbclass:**
```bitbake
# Custom class for deployment tasks

DEPLOY_DIR_CUSTOM = "${DEPLOY_DIR}/custom"

do_deploy_custom() {
    install -d ${DEPLOY_DIR_CUSTOM}
    install -m 0644 ${B}/output.bin ${DEPLOY_DIR_CUSTOM}/
}

addtask deploy_custom after do_compile before do_build
```

**Usage:**
```bitbake
inherit custom-deploy

do_compile:append() {
    # Generate output.bin
    generate_binary ${B}/output.bin
}
```

**Real-World Example - CUDA Application:**

```bitbake
SUMMARY = "CUDA-accelerated image processing"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=..."

SRC_URI = "git://github.com/example/cuda-app.git;protocol=https;branch=master"
SRCREV = "${AUTOREV}"
PV = "1.0+git${SRCPV}"

S = "${WORKDIR}/git"

# Inherit multiple classes
inherit cmake cuda systemd

# CMake configuration
EXTRA_OECMAKE = "\
    -DCUDA_TOOLKIT_ROOT_DIR=${STAGING_DIR_HOST}/usr/local/cuda \
    -DENABLE_TENSORRT=ON \
"

# CUDA class provides CUDA-specific variables
DEPENDS = "cuda-toolkit opencv"

# systemd integration
SYSTEMD_SERVICE:${PN} = "cuda-processor.service"

do_install:append() {
    # Install systemd service
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/cuda-processor.service ${D}${systemd_system_unitdir}/

    # Install CUDA kernels
    install -d ${D}${datadir}/cuda-app
    install -m 0644 ${B}/kernels/*.ptx ${D}${datadir}/cuda-app/
}

RDEPENDS:${PN} = "cuda-runtime libcudnn"
```

**Class vs Include Files:**

```bitbake
# Class (.bbclass) - provides functionality
inherit cmake

# Include (.inc) - shares variable definitions
require myapp.inc
include optional.inc  # Won't fail if missing
```

**Debugging Classes:**

```bash
# See which classes are inherited
bitbake -e myapp | grep "^inherit "

# View class file
cat meta/classes/cmake.bbclass

# See where a variable is defined
bitbake -e myapp | grep -B 5 "^EXTRA_OECMAKE="
```

**Class Inheritance Order:**

```bitbake
# Classes are processed in order
inherit autotools pkgconfig

# Later classes can override earlier ones
# Be careful with inheritance order
```

**Global vs Recipe Classes (Yocto 4.0+):**

```
classes/              # Legacy location
classes-recipe/       # Only for recipes (inherit)
classes-global/       # Globally applied (INHERIT)
```

```bitbake
# In local.conf or distro
INHERIT += "rm_work buildhistory"

# In recipe
inherit cmake systemd
```

**Key Points to Cover:**
- Classes provide reusable functionality
- inherit statement includes class in recipe
- Common classes: cmake, autotools, systemd, kernel
- Classes define standard tasks and variables
- Custom classes go in meta-layer/classes/
- Order of inheritance matters
- Classes reduce code duplication

**Follow-up Questions:**
1. What's the difference between inherit and require?
2. How would you create a custom class for your organization?

**Red Flags (Weak Answers):**
- Not knowing common classes (cmake, autotools)
- Manually implementing what classes provide
- Not understanding class provides tasks
- Confusing classes with include files
- Never looked at class implementation

---

### Q10: How do you create a custom image recipe? [Difficulty: Mid]

**Question:**
Walk through creating a custom image recipe from scratch. What are the key variables and how do you specify which packages to include?

**Expected Answer:**

**Image Recipe Basics:**

Image recipes define the contents and configuration of the root filesystem and final bootable image.

**Minimal Image Recipe:**

```bitbake
# meta-custom/recipes-images/images/custom-image-minimal.bb

SUMMARY = "Custom minimal image"
LICENSE = "MIT"

# Inherit image class
inherit core-image

# Packages to install
IMAGE_INSTALL = "\
    packagegroup-core-boot \
    ${CORE_IMAGE_EXTRA_INSTALL} \
"

# Image size (MB)
IMAGE_ROOTFS_SIZE ?= "8192"
IMAGE_ROOTFS_EXTRA_SPACE:append = "${@bb.utils.contains("DISTRO_FEATURES", "systemd", " + 4096", "", d)}"
```

**Complete Custom Image:**

```bitbake
# meta-custom/recipes-images/images/jetson-production-image.bb

SUMMARY = "Production image for Jetson devices"
DESCRIPTION = "Minimal production image with AI inference capabilities"
LICENSE = "MIT"

# Inherit core functionality
inherit core-image

# Base packages
IMAGE_INSTALL = "\
    packagegroup-core-boot \
    packagegroup-core-ssh-openssh \
    ${CORE_IMAGE_EXTRA_INSTALL} \
"

# Application packages
IMAGE_INSTALL += "\
    myapp \
    myapp-config \
    packagegroup-ai-inference \
"

# System utilities
IMAGE_INSTALL += "\
    htop \
    vim \
    tmux \
    wget \
    curl \
    rsync \
"

# Development tools (conditional)
IMAGE_INSTALL += "${@bb.utils.contains('IMAGE_FEATURES', 'tools-sdk', 'packagegroup-core-buildessential', '', d)}"

# Image features
IMAGE_FEATURES += "\
    ssh-server-openssh \
    read-only-rootfs \
    post-install-logging \
"

# Remove features for production
IMAGE_FEATURES:remove = "debug-tweaks"

# Root filesystem type
IMAGE_FSTYPES = "tar.bz2 ext4"

# Image size
IMAGE_ROOTFS_SIZE ?= "2048"
IMAGE_ROOTFS_EXTRA_SPACE = "524288"

# Root password (for production, use secure method)
# EXTRA_USERS_PARAMS = "usermod -P 'encryptedpassword' root;"

# Post-install scripts
ROOTFS_POSTPROCESS_COMMAND += "production_config; "

production_config() {
    # Remove unnecessary files
    rm -f ${IMAGE_ROOTFS}/etc/hostname

    # Set production hostname
    echo "jetson-production" > ${IMAGE_ROOTFS}/etc/hostname

    # Configure network
    install -d ${IMAGE_ROOTFS}/etc/systemd/network
    cat > ${IMAGE_ROOTFS}/etc/systemd/network/20-wired.network << EOF
[Match]
Name=eth0

[Network]
DHCP=yes
EOF

    # Disable unnecessary services
    rm -f ${IMAGE_ROOTFS}${systemd_system_unitdir}/multi-user.target.wants/bluetooth.service
}

# SDK generation
IMAGE_INSTALL:append = "${@bb.utils.contains('IMAGE_FEATURES', 'tools-sdk', ' packagegroup-core-standalone-sdk-target', '', d)}"

# License management
COPY_LIC_MANIFEST = "1"
COPY_LIC_DIRS = "1"
```

**Key Variables:**

**IMAGE_INSTALL:**
```bitbake
# Base definition
IMAGE_INSTALL = "packagegroup-core-boot"

# Add packages
IMAGE_INSTALL += "vim openssh python3"

# Append in local.conf (preserve user additions)
IMAGE_INSTALL:append = " mypackage"

# Machine-specific packages
IMAGE_INSTALL:append:jetson-nano = " cuda-runtime"
```

**IMAGE_FEATURES:**
```bitbake
# Common features
IMAGE_FEATURES = "\
    ssh-server-openssh \    # SSH server
    package-management \    # Package manager (e.g., rpm)
    debug-tweaks \          # Development conveniences
    tools-debug \           # Debugging tools
    tools-sdk \             # Development SDK
    read-only-rootfs \      # Read-only root filesystem
    empty-root-password \   # No root password (dev only!)
"

# Feature groups provided by core-image.bbclass
FEATURE_PACKAGES_ssh-server-openssh = "packagegroup-core-ssh-openssh"
FEATURE_PACKAGES_tools-debug = "packagegroup-core-tools-debug"
```

**IMAGE_FSTYPES:**
```bitbake
# Output formats
IMAGE_FSTYPES = "tar.bz2 ext4 wic"

# Machine-specific
IMAGE_FSTYPES:jetson-nano = "tegraflash"

# Multiple formats
IMAGE_FSTYPES = "ext4 tar.gz wic.bz2"
```

**Package Groups for Images:**

```bitbake
# meta-custom/recipes-core/packagegroups/packagegroup-ai-inference.bb

SUMMARY = "AI inference package group"
inherit packagegroup

RDEPENDS:${PN} = "\
    cuda-runtime \
    libcudnn \
    tensorrt \
    opencv \
    python3 \
    python3-numpy \
    python3-pillow \
"
```

**Image Hierarchy:**

```bitbake
# Extend existing image
require recipes-core/images/core-image-minimal.bb

IMAGE_INSTALL += "mypackage"

# Or inherit and extend
require recipes-core/images/core-image-base.bb

IMAGE_INSTALL:append = " custom-packages"
```

**Build and Deploy:**

```bash
# Build image
bitbake jetson-production-image

# Output location
ls tmp/deploy/images/jetson-nano/
# jetson-production-image-jetson-nano.tar.bz2
# jetson-production-image-jetson-nano.ext4
# jetson-production-image-jetson-nano.manifest

# Flash to SD card
sudo dd if=tmp/deploy/images/jetson-nano/jetson-production-image-jetson-nano.ext4 \
        of=/dev/sdX bs=4M status=progress
```

**Advanced Image Customization:**

**1. Post-Install Commands:**
```bitbake
ROOTFS_POSTPROCESS_COMMAND += "custom_setup; "

custom_setup() {
    # Runs in fakeroot after package installation

    # Create custom directories
    install -d ${IMAGE_ROOTFS}/data
    install -d ${IMAGE_ROOTFS}/models

    # Set permissions
    chmod 755 ${IMAGE_ROOTFS}/data

    # Create symlinks
    ln -sf /data ${IMAGE_ROOTFS}/var/app-data

    # Modify configurations
    sed -i 's/OPTION=.*/OPTION=value/' ${IMAGE_ROOTFS}/etc/config
}
```

**2. Pre-Install Commands:**
```bitbake
ROOTFS_PREPROCESS_COMMAND += "prepare_rootfs; "

prepare_rootfs() {
    # Runs before package installation
}
```

**3. User Management:**
```bitbake
inherit extrausers

EXTRA_USERS_PARAMS = "\
    useradd -p '' appuser; \
    usermod -P 'hashedpassword' root; \
    groupadd developers; \
    usermod -a -G developers appuser; \
"
```

**4. Read-Only Root Filesystem:**
```bitbake
IMAGE_FEATURES += "read-only-rootfs"

# Writable directories
VOLATILE_LOG_DIR = "yes"
```

**Multi-Configuration Image:**

```bitbake
# Build for multiple machines
BBMULTICONFIG = "jetson-nano jetson-xavier"

# Build
bitbake mc:jetson-nano:jetson-production-image
bitbake mc:jetson-xavier:jetson-production-image
```

**Key Points to Cover:**
- Image recipes inherit core-image or image
- IMAGE_INSTALL specifies packages
- IMAGE_FEATURES adds functionality groups
- IMAGE_FSTYPES controls output format
- Use packagegroups to organize related packages
- ROOTFS_POSTPROCESS_COMMAND for customization
- Read-only rootfs for production
- Proper user management for security

**Follow-up Questions:**
1. How would you create a minimal rescue image?
2. What's the difference between IMAGE_INSTALL and PACKAGE_INSTALL?

**Red Flags (Weak Answers):**
- Not understanding IMAGE_INSTALL vs IMAGE_FEATURES
- Hardcoding package lists instead of using packagegroups
- Not knowing about ROOTFS_POSTPROCESS_COMMAND
- Using debug-tweaks in production
- Not understanding image inheritance

---

### Q11: Explain devshell and how to use it for debugging [Difficulty: Mid]

**Question:**
What is devshell and how do you use it to debug recipe build issues? Provide a practical example.

**Expected Answer:**

**devshell Overview:**

devshell is an interactive development shell that provides the complete build environment for a recipe, allowing manual execution and debugging of build steps.

**Basic Usage:**

```bash
# Open devshell for a recipe
bitbake -c devshell recipe-name

# Example
bitbake -c devshell opencv
```

**What devshell Provides:**

1. **Pre-configured Environment:**
   - All BitBake variables set correctly
   - Cross-compilation toolchain configured
   - Dependencies staged
   - Working directory set to ${S}

2. **Available Variables:**
```bash
# In devshell:
echo $S          # Source directory
echo $B          # Build directory
echo $D          # Install destination
echo $WORKDIR    # Work directory

echo $CC         # C compiler (cross-compiler)
echo $CXX        # C++ compiler
echo $LD         # Linker
echo $AR         # Archiver

echo $CFLAGS     # Compiler flags
echo $LDFLAGS    # Linker flags
echo $CXXFLAGS   # C++ flags

echo $PKG_CONFIG_PATH      # pkg-config search path
echo $STAGING_INCDIR       # Header files
echo $STAGING_LIBDIR       # Libraries
```

**Practical Debugging Example:**

**Scenario:** Recipe fails during compilation

```bash
# Build fails
bitbake myapp
ERROR: myapp-1.0-r0 do_compile: oe_runmake failed

# Open devshell
bitbake -c devshell myapp
```

**In devshell:**

```bash
# 1. Check environment
pwd  # Should be in ${S}
ls   # View source files

# 2. Try manual compilation
cd $B  # Go to build directory

# 3. Run configure manually
../configure --prefix=/usr --enable-features

# 4. Try compilation with verbose output
make VERBOSE=1

# Error: undefined reference to `SSL_connect'
# Solution: Missing -lssl

# 5. Check pkg-config
pkg-config --libs openssl
# Output: -lssl -lcrypto

# 6. Verify library exists
ls $STAGING_LIBDIR/libssl*

# 7. Test fixed compilation
make LDFLAGS="$LDFLAGS -lssl -lcrypto"

# Success! Now update recipe
```

**Update recipe based on devshell findings:**

```bitbake
do_compile() {
    oe_runmake EXTRA_LDFLAGS="-lssl -lcrypto"
}

# Or add to DEPENDS
DEPENDS += "openssl"
```

**Common devshell Use Cases:**

**1. Testing Build Commands:**
```bash
bitbake -c devshell myapp

# In devshell:
cd $B
cmake $S -DENABLE_FEATURE=ON
make -j4
make install DESTDIR=$D

# If successful, add to recipe
```

**2. Investigating Missing Headers:**
```bash
# In devshell:
echo $CFLAGS
# Check include paths

find $STAGING_INCDIR -name "needed-header.h"
pkg-config --cflags dependency

# Fix by adding DEPENDS in recipe
```

**3. Checking Patch Application:**
```bash
bitbake -c devshell myapp

# In devshell:
cd $S
cat .pc/applied-patches

# Check if patches applied correctly
git diff  # If source is git repo
quilt series  # List patches
quilt push  # Apply next patch
```

**4. Testing Python Recipes:**
```bash
bitbake -c devshell python3-mypackage

# In devshell:
cd $S
python3 setup.py build
python3 setup.py install --prefix=/usr --root=$D

# Test import
python3 -c "import mypackage; print(mypackage.version)"
```

**5. Debugging Cross-Compilation:**
```bash
bitbake -c devshell myapp

# In devshell:
echo $CC
# Output: aarch64-poky-linux-gcc --sysroot=...

file $B/compiled-binary
# Output: ELF 64-bit LSB executable, ARM aarch64, ...

# Verify it's for target, not host
```

**Advanced devshell Techniques:**

**1. Execute Specific Task Environment:**
```bash
# Devshell after do_configure
bitbake -c devshell myapp

# In devshell, manually run:
run_do_configure  # Defined in temp/run.do_configure.*
```

**2. Modify and Test:**
```bash
bitbake -c devshell myapp

# In devshell:
cd $S
vim src/main.c  # Make changes

# Rebuild
cd $B
make

# Test installation
make install DESTDIR=$D
ls $D/usr/bin/

# If working, create patch
cd $S
git diff > /tmp/fix.patch
# Copy to recipe directory
```

**3. Debugging with GDB:**
```bash
bitbake -c devshell myapp

# In devshell (for native binaries):
cd $B
gdb ./myapp
(gdb) run
(gdb) backtrace
```

**4. Package Inspection:**
```bash
bitbake -c devshell myapp

# After do_install:
cd $D
find . -type f  # See what gets installed
tree            # View directory structure

# Check package splitting
cd $WORKDIR/packages-split/
ls myapp myapp-dev myapp-dbg
```

**Exiting and Cleaning:**

```bash
# Exit devshell
exit

# Clean and rebuild with fixes
bitbake -c cleansstate myapp
bitbake myapp
```

**Alternative: devtool modify**

For more integrated development:

```bash
# Better alternative for extensive modifications
devtool modify myapp

# Creates workspace with git
cd workspace/sources/myapp

# Make changes
vim src/main.c
git commit -am "Fix issue"

# Build with changes
bitbake myapp

# Update recipe with patches
devtool update-recipe myapp
```

**devshell vs devtool:**

| Aspect | devshell | devtool modify |
|--------|----------|----------------|
| Purpose | Quick debugging | Extended development |
| Source control | No | Git integrated |
| Patch creation | Manual | Automatic |
| Persistence | Session only | Workspace |
| Best for | One-off fixes | Feature development |

**Key Points to Cover:**
- devshell provides complete build environment
- All BitBake variables available
- Cross-compilation tools configured
- Can manually run build steps
- Great for debugging build failures
- Environment matches actual build
- Changes not persistent (use devtool for that)

**Follow-up Questions:**
1. How would you debug a recipe that builds but installs to wrong location?
2. What's the difference between devshell and running commands in tmp/work/?

**Red Flags (Weak Answers):**
- Never used devshell for debugging
- Not knowing environment variables available
- Modifying source in place without version control
- Not understanding cross-compilation environment
- Unable to manually reproduce build steps

---

### Q12: How do you optimize build times in Yocto? [Difficulty: Senior]

**Question:**
What strategies and techniques would you employ to reduce Yocto build times in a development environment and CI/CD pipeline?

**Expected Answer:**

**Build Time Optimization Strategies:**

**1. Shared State Cache (sstate-cache):**

**Local Configuration:**
```bitbake
# conf/local.conf
SSTATE_DIR = "/path/to/shared/sstate-cache"

# Use NFS or network storage for team sharing
SSTATE_DIR = "/mnt/nfs/yocto-sstate"
```

**Network Mirror:**
```bitbake
# HTTP server for sstate
SSTATE_MIRRORS = "\
    file://.* http://sstate-server.company.com/PATH;downloadfilename=PATH \n \
    file://.* file:///mnt/backup-sstate/PATH \n \
"
```

**2. Shared Downloads Directory:**

```bitbake
# Share downloaded sources across builds
DL_DIR = "/path/to/shared/downloads"

# Network mirror for downloads
PREMIRRORS:prepend = "\
    git://.*/.* http://source-mirror.company.com/ \n \
    ftp://.*/.* http://source-mirror.company.com/ \n \
    http://.*/.* http://source-mirror.company.com/ \n \
    https://.*/.* http://source-mirror.company.com/ \n \
"
```

**3. Parallel Building:**

```bitbake
# conf/local.conf

# Number of BitBake tasks to run in parallel
# Rule of thumb: # of CPU cores
BB_NUMBER_THREADS = "8"

# Number of Make jobs per task
# Rule of thumb: 1.5-2x # of CPU cores
PARALLEL_MAKE = "-j 16"

# Limit per recipe (some recipes don't support parallel make)
PARALLEL_MAKE:pn-problem-recipe = "-j 1"
```

**4. Incremental Builds:**

```bitbake
# Don't remove work directories after build
# Allows incremental compilation

# Comment out or remove:
# INHERIT += "rm_work"

# Or exclude specific recipes:
RM_WORK_EXCLUDE += "myapp opencv linux-tegra"
```

**5. Use buildhistory:**

```bitbake
# Track what changed between builds
INHERIT += "buildhistory"
BUILDHISTORY_COMMIT = "1"
```

**6. Optimize Disk I/O:**

```bash
# Use SSD for build directory
# Or tmpfs for tmp/work
mount -t tmpfs -o size=50G tmpfs /path/to/build/tmp

# In local.conf:
# Be cautious with RAM usage
```

**7. Hash Equivalence Server:**

```bitbake
# Share build artifacts across machines
# Even if paths differ
BB_HASHSERVE = "auto"
BB_SIGNATURE_HANDLER = "OEEquivHash"

# Dedicated server for team
BB_HASHSERVE = "hashserv.company.com:8686"
```

**8. Target Only What's Needed:**

```bash
# Don't build SDK if not needed
# Don't build all MACHINES

# Build specific recipe
bitbake myapp

# Build only what changed
bitbake myapp --runall=fetch  # Fetch mode
```

**9. Recipe-Specific Optimization:**

```bitbake
# Disable unnecessary features
PACKAGECONFIG:remove = "doc examples tests"

# Skip unused packages
PACKAGE_EXCLUDE = "package-doc package-locale-*"

# Disable debug symbols in development
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"
```

**10. Image Optimization:**

```bitbake
# Smaller images build faster
IMAGE_FEATURES:remove = "tools-debug tools-profile tools-sdk"

# Simple image format
IMAGE_FSTYPES = "tar.gz"  # Instead of multiple formats
```

**Complete Optimized Configuration:**

```bitbake
# conf/local.conf - Optimized for development

# Parallelization (16-core machine)
BB_NUMBER_THREADS = "16"
PARALLEL_MAKE = "-j 24"

# Shared state and downloads
SSTATE_DIR = "/mnt/ssd/yocto-shared/sstate-cache"
DL_DIR = "/mnt/ssd/yocto-shared/downloads"

# Hash equivalence
BB_HASHSERVE = "auto"
BB_SIGNATURE_HANDLER = "OEEquivHash"

# Network mirrors
SOURCE_MIRROR_URL = "http://source-mirror.lan/"
INHERIT += "own-mirrors"
BB_GENERATE_MIRROR_TARBALLS = "1"

SSTATE_MIRRORS = "\
    file://.* http://sstate-cache.lan/PATH;downloadfilename=PATH \n \
"

# Keep work directories for incremental builds
# INHERIT += "rm_work"  # Commented out for dev
RM_WORK_EXCLUDE += "myapp important-package"

# Build history
INHERIT += "buildhistory"
BUILDHISTORY_COMMIT = "1"

# Optimize features
EXTRA_IMAGE_FEATURES:remove = "debug-tweaks tools-debug"

# Fast image format
IMAGE_FSTYPES = "tar.bz2"
```

**CI/CD Optimization:**

```yaml
# GitLab CI example
variables:
  SSTATE_DIR: "/cache/sstate-cache"
  DL_DIR: "/cache/downloads"
  BB_NUMBER_THREADS: "8"
  PARALLEL_MAKE: "-j 16"

cache:
  key: yocto-build
  paths:
    - sstate-cache/
    - downloads/

before_script:
  - mkdir -p ${SSTATE_DIR} ${DL_DIR}

build:
  script:
    - source oe-init-build-env
    - bitbake core-image-minimal
  artifacts:
    paths:
      - build/tmp/deploy/images/
```

**Measurement and Monitoring:**

```bash
# Build with timing
time bitbake core-image-minimal

# Per-task timing
bitbake -g core-image-minimal -u taskexp

# Build statistics
buildstats.py tmp/buildstats/

# Find slow recipes
find tmp/buildstats/ -name "*" -type f -exec du -h {} + | sort -rh | head -20
```

**11. Docker/Container Optimization:**

```dockerfile
# Use buildtools-tarball for consistent environment
FROM crops/poky:ubuntu-18.04

# Mount caches as volumes
VOLUME ["/sstate-cache", "/downloads"]

# Pre-populated base image
RUN bitbake buildtools-tarball
```

**12. Conditional Building:**

```bitbake
# Skip native builds if not needed
ASSUME_PROVIDED += "some-native-tool"

# Use prebuild toolchains
TCMODE = "external-arm"
```

**Performance Comparison:**

| Optimization | Time Saving | Effort |
|--------------|-------------|--------|
| sstate-cache (first build) | 0% | Low |
| sstate-cache (rebuild) | 70-90% | Low |
| Parallel building | 50-70% | Low |
| Shared downloads | 10-20% | Low |
| Hash equivalence | 20-40% | Medium |
| Incremental builds | 30-50% | Medium |
| SSD storage | 20-30% | Low-Medium |

**Best Practices:**

```bitbake
# 1. Always use shared state cache
SSTATE_DIR = "/shared/path"

# 2. Network mirrors for reliability
PREMIRRORS + SSTATE_MIRRORS

# 3. Appropriate parallelization
BB_NUMBER_THREADS = "# of cores"
PARALLEL_MAKE = "-j cores * 1.5"

# 4. Keep work dirs during development
# Remove in production CI

# 5. Monitor build stats
INHERIT += "buildstats buildhistory"

# 6. Use hash equivalence for team builds
BB_HASHSERVE = "server:8686"
```

**Common Pitfalls:**

```bitbake
# Don't do this:
BB_NUMBER_THREADS = "32"  # On 8-core machine (thrashing)
PARALLEL_MAKE = "-j 64"   # Excessive

# Don't do this:
SSTATE_DIR = "/tmp/sstate"  # Temporary, gets deleted

# Don't do this:
# Mix different Yocto versions in same sstate-cache
```

**Key Points to Cover:**
- sstate-cache is most impactful optimization
- Parallel building requires proper tuning
- Shared resources across team/CI
- Hash equivalence for path-independent caching
- Incremental builds for development
- Measure and monitor build times
- Different strategies for dev vs CI

**Follow-up Questions:**
1. How would you set up a shared sstate-cache server for a team of 10 developers?
2. What would cause sstate-cache to be invalidated unnecessarily?

**Red Flags (Weak Answers):**
- Not using sstate-cache
- Not knowing about hash equivalence
- Setting excessive parallelization
- Not understanding incremental builds
- Never measured build performance
- Using rm_work during active development

---

### Q13: Explain SRCPV and why it's used with git recipes [Difficulty: Mid]

**Question:**
What is SRCPV and how does it work with Git-based recipes? Why is it important for versioning?

**Expected Answer:**

**SRCPV Overview:**

SRCPV is a special variable that provides version information from source control (primarily Git) for use in package versioning.

**Basic Usage:**

```bitbake
SUMMARY = "Application from Git"
LICENSE = "MIT"

SRC_URI = "git://github.com/example/myapp.git;protocol=https;branch=master"
SRCREV = "abc123def456..."

# Use SRCPV in PV
PV = "1.0+git${SRCPV}"

S = "${WORKDIR}/git"
```

**What SRCPV Provides:**

```bitbake
# SRCPV expands to: AUTOINC+gitSHORTHASH
# Example: AUTOINC+gitabc123d

# Final PV becomes:
PV = "1.0+git${SRCPV}"
# Results in: 1.0+gitAUTOINC+abc123d
```

**SRCPV Components:**

1. **AUTOINC**: Auto-incrementing counter
   - Ensures newer commits have higher version numbers
   - Managed in source-revision cache

2. **gitSHORTHASH**: Shortened commit hash
   - First 7-10 characters of commit
   - Identifies exact source version

**Why SRCPV is Important:**

**1. Package Versioning:**
```bitbake
# Without SRCPV
PV = "1.0"  # Same version for all commits

# With SRCPV
PV = "1.0+git${SRCPV}"
# 1.0+gitAUTOINC+abc123d (commit abc123...)
# 1.0+gitAUTOINC+def456e (commit def456...)
# Package manager sees these as different versions
```

**2. Reproducible Builds:**
```bitbake
# Package name includes git hash
# myapp-1.0+gitAUTOINC+abc123d-r0_cortexa57.rpm

# Easy to trace exactly which commit was built
```

**3. Version Ordering:**
```bitbake
# AUTOINC ensures proper ordering:
# 1.0+git0+abc123d (older commit)
# 1.0+git1+def456e (newer commit)
# 1.0+git2+ghi789f (newest commit)

# Package manager correctly handles upgrades
```

**Complete Example:**

```bitbake
SUMMARY = "Development version from Git"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=..."

# Git source
SRC_URI = "git://git.example.com/myapp.git;protocol=ssh;branch=develop"

# Specific commit for reproducibility
SRCREV = "d4f6e8a2b1c3d5e7f9a0b2c4d6e8f0a1b3c5d7e9"

# Version with git info
PV = "2.0+git${SRCPV}"

S = "${WORKDIR}/git"

inherit cmake

DEPENDS = "openssl zlib"
```

**Multiple Git Repositories:**

```bitbake
SRC_URI = "\
    git://github.com/example/main.git;protocol=https;branch=master;name=main;destsuffix=git/main \
    git://github.com/example/plugin.git;protocol=https;branch=master;name=plugin;destsuffix=git/plugin \
"

SRCREV_main = "abc123..."
SRCREV_plugin = "def456..."

# SRCPV with multiple repos
PV = "1.0+git${SRCPV}"
# Includes information from primary (first) repo
```

**Using AUTOREV:**

```bitbake
# Development: always use latest commit
SRCREV = "${AUTOREV}"
PV = "1.0+git${SRCPV}"

# SRCPV updates automatically with new commits
# Results in: 1.0+gitAUTOINC+<latest-hash>
```

**Production vs Development:**

**Development Recipe:**
```bitbake
# myapp_git.bb
SRCREV = "${AUTOREV}"
PV = "2.0+git${SRCPV}"

DEFAULT_PREFERENCE = "-1"  # Don't use by default
```

**Production Recipe:**
```bitbake
# myapp_2.0.bb
SRC_URI = "https://releases.example.com/myapp-2.0.tar.gz"
# No SRCPV needed for tarballs
```

**Version String Examples:**

```bitbake
# Tarball version
PV = "1.0"
# Package: myapp-1.0-r0

# Git with SRCPV
PV = "1.0+git${SRCPV}"
# Package: myapp-1.0+gitAUTOINC+abc123d-r0

# Git with date
PV = "1.0+git${@time.strftime('%Y%m%d',time.gmtime())}"
# Package: myapp-1.0+git20240115-r0

# Complex versioning
PV = "1.0+${@'git' + d.getVar('SRCPV', True).replace('AUTOINC', '')}
# Custom format
```

**Package Filename with SRCPV:**

```bash
# Built package includes git hash
tmp/deploy/rpm/cortexa57/myapp-1.0+git0+abc123d-r0.cortexa57.rpm

# Easy to identify:
# - Base version: 1.0
# - Git-based: +git
# - Counter: 0
# - Commit: abc123d
# - Recipe revision: r0
```

**SRCPV in Package Feeds:**

```bitbake
# Package feed organization
package-feed/
├── myapp-1.0+git0+abc123d-r0.rpm
├── myapp-1.0+git1+def456e-r0.rpm  # Newer
└── myapp-1.0+git2+ghi789f-r0.rpm  # Newest

# Package manager prefers highest version
dnf install myapp  # Installs git2+ghi789f
```

**Debugging SRCPV:**

```bash
# Check SRCPV expansion
bitbake -e myapp | grep "^PV="
# PV="1.0+git0+abc123d"

bitbake -e myapp | grep "^SRCPV="
# SRCPV="0+gitabc123d"

# Check SRCREV
bitbake -e myapp | grep "^SRCREV="
# SRCREV="abc123def456..."
```

**Source Revision Cache:**

```bash
# AUTOINC counter stored in:
build/cache/bb_persist_data.sqlite3

# Query counter
sqlite3 cache/bb_persist_data.sqlite3 "SELECT * FROM BB_URI_HEADREVS"
```

**Key Points to Cover:**
- SRCPV creates version from git commit
- AUTOINC provides incrementing counter
- Essential for proper package versioning
- Enables traceability to exact commit
- Package managers handle versions correctly
- Use with git:// SRC_URI
- Not needed for tarball releases

**Follow-up Questions:**
1. How would you find which git commit is in a deployed package?
2. What happens to AUTOINC counter when you clean the build?

**Red Flags (Weak Answers):**
- Not using SRCPV with git recipes
- Not understanding AUTOINC purpose
- Using static PV with SRCREV
- Not knowing how to trace packages to commits
- Confusing SRCPV with SRCREV

---

### Q14: How do you handle recipe dependencies that are not in the same layer? [Difficulty: Mid]

**Question:**
Explain how to manage dependencies across different layers, including layer priorities and BBFILES_DYNAMIC.

**Expected Answer:**

**Cross-Layer Dependencies:**

**Basic Scenario:**

```
poky/meta/                    # Core layer
meta-openembedded/meta-oe/   # OE layer
meta-custom/                 # Your layer
```

**Layer Configuration:**

**meta-custom/conf/layer.conf:**
```bitbake
# Layer identity
BBPATH .= ":${LAYERDIR}"

BBFILES += "${LAYERDIR}/recipes-*/*/*.bb \
            ${LAYERDIR}/recipes-*/*/*.bbappend"

BBFILE_COLLECTIONS += "meta-custom"
BBFILE_PATTERN_meta-custom = "^${LAYERDIR}/"
BBFILE_PRIORITY_meta-custom = "10"

# Layer dependencies
LAYERDEPENDS_meta-custom = "core openembedded-layer"

# Layer compatibility
LAYERSERIES_COMPAT_meta-custom = "kirkstone langdale"
```

**conf/bblayers.conf:**
```bitbake
BBLAYERS ?= " \
  /path/to/poky/meta \
  /path/to/poky/meta-poky \
  /path/to/meta-openembedded/meta-oe \
  /path/to/meta-openembedded/meta-python \
  /path/to/meta-custom \
"
```

**Layer Priority and Recipe Selection:**

**Scenario:** Multiple layers have same recipe

```
meta-oe/recipes-support/opencv/opencv_4.5.bb
meta-custom/recipes-support/opencv/opencv_4.5.bb
```

**Priority determines which is used:**
```bitbake
# In layer.conf
BBFILE_PRIORITY_meta-oe = "5"
BBFILE_PRIORITY_meta-custom = "10"  # Higher priority wins

# meta-custom/opencv_4.5.bb will be used
```

**Check which recipe is selected:**
```bash
bitbake-layers show-recipes opencv
# Shows: opencv:
#          meta-custom  4.5
#          meta-oe      4.5
```

**Cross-Layer Dependencies in Recipes:**

**Simple Dependency:**
```bitbake
# meta-custom/recipes-apps/myapp/myapp.bb

DEPENDS = "opencv python3 protobuf"

# BitBake searches all layers for these recipes
# No special handling needed if layers are in BBLAYERS
```

**LAYERDEPENDS:**

**Declare layer-level dependencies:**
```bitbake
# meta-custom/conf/layer.conf

# Requires specific layers
LAYERDEPENDS_meta-custom = "core meta-oe meta-python"

# With version requirements
LAYERDEPENDS_meta-custom = "core:15 meta-oe:1"
```

**Error if dependency missing:**
```bash
ERROR: Layer 'meta-custom' depends on layer 'meta-oe', but it is not included in BBLAYERS
```

**BBFILES_DYNAMIC - Conditional Dependencies:**

**Problem:** Optional dependency on another layer

```bitbake
# meta-custom/conf/layer.conf

# Conditional recipes - only parsed if meta-python exists
BBFILES_DYNAMIC += "\
    meta-python:${LAYERDIR}/dynamic-layers/meta-python/recipes-*/*/*.bb \
    meta-python:${LAYERDIR}/dynamic-layers/meta-python/recipes-*/*/*.bbappend \
"
```

**Directory Structure:**
```
meta-custom/
├── conf/
│   └── layer.conf
├── recipes-core/
│   └── ...
└── dynamic-layers/
    ├── meta-python/
    │   └── recipes-apps/
    │       └── python-myapp/
    │           └── python3-myapp.bb
    └── meta-qt5/
        └── recipes-apps/
            └── myapp-gui/
                └── myapp-gui.bb
```

**Benefits:**
- Recipes only parsed if dependency layer exists
- No errors if optional layer missing
- Clean separation of optional features

**Example:**
```bitbake
# meta-custom/conf/layer.conf

BBFILES_DYNAMIC += "\
    qt5-layer:${LAYERDIR}/dynamic-layers/meta-qt5/recipes-*/*/*.bb \
    openembedded-layer:${LAYERDIR}/dynamic-layers/meta-oe/recipes-*/*/*.bbappend \
    meta-python:${LAYERDIR}/dynamic-layers/meta-python/recipes-*/*/*.bb \
"

# Only if meta-qt5 is in BBLAYERS:
# dynamic-layers/meta-qt5/* recipes are parsed
```

**Extending Recipes from Other Layers:**

**bbappend files:**

```bitbake
# meta-custom/recipes-support/opencv/opencv_%.bbappend

FILESEXTRAHPATH:prepend := "${THISDIR}/${PN}:"

# Add custom patches
SRC_URI += "file://custom-optimization.patch"

# Enable CUDA for Jetson
PACKAGECONFIG:append:tegra = " cuda"

DEPENDS:append:tegra = " cuda-toolkit"
```

**Directory structure:**
```
meta-custom/
└── recipes-support/
    └── opencv/
        ├── opencv_%.bbappend  # Applies to all versions
        └── opencv/
            └── custom-optimization.patch
```

**Version-Specific bbappend:**
```bitbake
# opencv_4.%.bbappend - applies to 4.x versions
# opencv_4.6.%.bbappend - applies to 4.6.x versions
# opencv_4.6.0.bbappend - applies only to 4.6.0
```

**PREFERRED_PROVIDER Across Layers:**

```bitbake
# Multiple layers provide virtual/kernel
# meta-tegra: linux-tegra
# meta-yocto-bsp: linux-yocto

# In conf/local.conf or machine config:
PREFERRED_PROVIDER_virtual/kernel = "linux-tegra"
```

**Real-World Example - Jetson Support:**

```bitbake
# meta-custom/conf/layer.conf

BBPATH .= ":${LAYERDIR}"
BBFILES += "${LAYERDIR}/recipes-*/*/*.bb ${LAYERDIR}/recipes-*/*/*.bbappend"

BBFILE_COLLECTIONS += "meta-custom"
BBFILE_PATTERN_meta-custom = "^${LAYERDIR}/"
BBFILE_PRIORITY_meta-custom = "10"

# Dependencies
LAYERDEPENDS_meta-custom = "core meta-tegra openembedded-layer"

# Compatibility
LAYERSERIES_COMPAT_meta-custom = "kirkstone"

# Optional Python support
BBFILES_DYNAMIC += "\
    meta-python:${LAYERDIR}/dynamic-layers/meta-python/recipes-*/*/*.bb \
"
```

**Debugging Layer Issues:**

```bash
# Show all layers
bitbake-layers show-layers

# Show layer dependencies
bitbake-layers show-dependencies

# Show where recipe comes from
bitbake-layers show-recipes opencv
bitbake-layers show-recipes -f opencv

# Show all versions
bitbake-layers show-recipes

# Show bbappends
bitbake-layers show-appends opencv

# Show cross-layer dependencies
bitbake -g core-image-minimal
# Generates: pn-buildlist, pn-depends.dot
```

**Common Issues and Solutions:**

**Issue 1: Recipe not found**
```bash
ERROR: Nothing PROVIDES 'mypackage'
```

**Solution:**
```bash
# Check if layer in BBLAYERS
bitbake-layers show-layers | grep meta-custom

# Check recipe path matches BBFILES
# Verify in layer.conf
```

**Issue 2: Wrong recipe version selected**
```bash
# Want 4.6 but getting 4.5
```

**Solution:**
```bitbake
# Set in local.conf
PREFERRED_VERSION_opencv = "4.6%"

# Or check layer priority
bitbake-layers show-recipes -f opencv
```

**Issue 3: bbappend not applied**
```bash
# Patch not being applied
```

**Solution:**
```bash
# Check bbappend matches recipe version
bitbake-layers show-appends opencv

# Verify FILESEXTRAPATHS
bitbake -e opencv | grep "^FILESEXTRAPATHS="
```

**Key Points to Cover:**
- BBLAYERS determines which layers are active
- LAYERDEPENDS enforces layer requirements
- BBFILE_PRIORITY resolves recipe conflicts
- BBFILES_DYNAMIC for optional dependencies
- bbappend extends recipes from other layers
- Use bitbake-layers for debugging
- Dynamic layers for clean optional features

**Follow-up Questions:**
1. How would you handle a dependency that might be in different layers depending on configuration?
2. What happens if two layers have the same BBFILE_PRIORITY?

**Red Flags (Weak Answers):**
- Not understanding layer priority
- Never used BBFILES_DYNAMIC
- Not knowing bitbake-layers commands
- Hardcoding layer paths
- Not declaring LAYERDEPENDS
- Not understanding bbappend version matching

---

### Q15: Explain the do_populate_sysroot task and staging [Difficulty: Senior]

**Question:**
What is the do_populate_sysroot task and how does the staging mechanism work in Yocto? Why is it important for cross-compilation?

**Expected Answer:**

**do_populate_sysroot Overview:**

The do_populate_sysroot task stages files (headers, libraries, pkg-config files) from a recipe's output so other recipes can use them during compilation.

**Staging Purpose:**

In cross-compilation, recipes need access to:
- Headers (*.h)
- Development libraries (*.so, *.a, *.la)
- pkg-config files (*.pc)
- CMake config files
- Development tools

**How Staging Works:**

```
Recipe A (library)
    ↓ do_install
    ${D}/usr/lib/libfoo.so
    ${D}/usr/include/foo.h
    ↓ do_populate_sysroot
    ${STAGING_DIR}/usr/lib/libfoo.so
    ${STAGING_DIR}/usr/include/foo.h
    ↓
Recipe B (depends on A)
    Compiles using staged files
    ${STAGING_INCDIR}/foo.h
    ${STAGING_LIBDIR}/libfoo.so
```

**Key Staging Directories:**

```bitbake
# Target architecture sysroot
STAGING_DIR_TARGET = "${TMPDIR}/sysroots/${MACHINE}"
#example: tmp/sysroots/jetson-nano/

# Host (build machine) sysroot
STAGING_DIR_NATIVE = "${TMPDIR}/sysroots-components/x86_64"

# Cross-compilation tools
STAGING_DIR_CROSS = "${TMPDIR}/sysroots-components/x86_64-${MACHINE}"

# Convenience variables
STAGING_INCDIR = "${STAGING_DIR_TARGET}/usr/include"
STAGING_LIBDIR = "${STAGING_DIR_TARGET}/usr/lib"
STAGING_DATADIR = "${STAGING_DIR_TARGET}/usr/share"
```

**Task Dependencies:**

```bitbake
# Automatic dependency chain
do_populate_sysroot[depends] = "${PN}:do_install"

# Other recipes depend on this
do_configure[depends] = "dependency:do_populate_sysroot"
```

**What Gets Staged:**

```bitbake
# By default (from staging.bbclass):
SYSROOT_DIRS = "\
    ${includedir} \
    ${libdir} \
    ${base_libdir} \
    ${nonarch_base_libdir} \
    ${datadir} \
    ${systemd_system_unitdir} \
"

# Additional directories
SYSROOT_DIRS:append = " ${bindir}"
```

**Example Recipe with Staging:**

**Library Recipe (libfoo):**
```bitbake
SUMMARY = "Example library"
LICENSE = "MIT"

SRC_URI = "https://example.com/libfoo-${PV}.tar.gz"

inherit cmake

do_install() {
    # Install to ${D}
    install -d ${D}${libdir}
    install -m 0755 ${B}/libfoo.so.1.0 ${D}${libdir}/
    ln -sf libfoo.so.1.0 ${D}${libdir}/libfoo.so

    # Development files
    install -d ${D}${includedir}/foo
    install -m 0644 ${S}/include/*.h ${D}${includedir}/foo/

    # pkg-config file
    install -d ${D}${libdir}/pkgconfig
    install -m 0644 ${B}/foo.pc ${D}${libdir}/pkgconfig/
}

# do_populate_sysroot runs automatically after do_install
# Stages to ${STAGING_DIR_TARGET}/usr/lib, /usr/include, etc.
```

**Application Recipe (depends on libfoo):**
```bitbake
SUMMARY = "Application using libfoo"
LICENSE = "MIT"

# Dependency triggers do_populate_sysroot
DEPENDS = "libfoo"

inherit cmake

do_configure() {
    # pkg-config finds staged files
    pkg-config --cflags foo
    # Returns: -I/path/to/sysroot/usr/include/foo

    pkg-config --libs foo
    # Returns: -L/path/to/sysroot/usr/lib -lfoo

    # CMake uses CMAKE_FIND_ROOT_PATH pointing to sysroot
    cmake ${S}
}

do_compile() {
    # Compiler automatically uses staged files via CFLAGS/LDFLAGS
    oe_runmake
}
```

**Staging Manifest:**

Each recipe creates staging manifest:
```bash
# Location
tmp/sysroots-components/cortexa57/libfoo/sysroot-providers/libfoo

# Contents
/usr/include/foo/foo.h
/usr/lib/libfoo.so
/usr/lib/libfoo.so.1.0
/usr/lib/pkgconfig/foo.pc
```

**Cross-Compilation Setup:**

```bitbake
# Compiler flags automatically include staged directories
TARGET_CFLAGS = "-I${STAGING_INCDIR} ..."
TARGET_LDFLAGS = "-L${STAGING_LIBDIR} ..."

# pkg-config configured to search sysroot
PKG_CONFIG_SYSROOT_DIR = "${STAGING_DIR_TARGET}"
PKG_CONFIG_PATH = "${STAGING_DATADIR}/pkgconfig"
PKG_CONFIG_LIBDIR = "${STAGING_LIBDIR}/pkgconfig"
```

**sysroot-destdir (Intermediate Staging):**

```bitbake
# Recipes first populate intermediate directory
SYSROOT_DESTDIR = "${WORKDIR}/sysroot-destdir"

# do_populate_sysroot process:
# 1. Copy files from ${D} to ${SYSROOT_DESTDIR}
# 2. Process files (fix paths, etc.)
# 3. Deploy to actual sysroot via sstate
```

**Custom Staging:**

```bitbake
# Add custom directories to staging
SYSROOT_DIRS:append = " ${datadir}/myapp/configs"

# Manual staging in recipe
do_install:append() {
    # These will be automatically staged
    install -d ${D}${datadir}/myapp
    install -m 0644 ${S}/data/* ${D}${datadir}/myapp/
}

# Access in dependent recipe
do_configure:prepend() {
    cp ${STAGING_DATADIR}/myapp/config.xml ${S}/
}
```

**Native Sysroot:**

```bitbake
# Native recipes (build tools)
inherit native

# Staged to different location
# ${STAGING_DIR_NATIVE}${bindir}
# Example: tmp/sysroots-components/x86_64/cmake-native/usr/bin/cmake

# Used during build
do_configure() {
    cmake ${S}  # Uses cmake from native sysroot
}
```

**Debugging Staging Issues:**

```bash
# Check what's staged for a recipe
ls tmp/sysroots-components/cortexa57/libfoo/

# View staging manifest
cat tmp/sysroots-components/cortexa57/libfoo/sysroot-providers/libfoo

# Check sysroot contents
ls tmp/sysroots/jetson-nano/usr/lib
ls tmp/sysroots/jetson-nano/usr/include

# See staging task output
cat tmp/work/cortexa57-poky-linux/libfoo/1.0-r0/temp/log.do_populate_sysroot

# Verify dependencies
bitbake -g myapp
# Check task-depends.dot for do_populate_sysroot dependencies
```

**Common Issues:**

**Issue 1: Header not found**
```
fatal error: foo.h: No such file or directory
```

**Check:**
```bash
# Verify dependency staged the header
bitbake -c populate_sysroot libfoo
ls tmp/sysroots/jetson-nano/usr/include/

# Check if dependency declared
grep "^DEPENDS=" recipes/myapp/myapp.bb
```

**Issue 2: Wrong library version**
```
# Multiple versions staged
```

**Solution:**
```bash
# Clean sysroot
bitbake -c cleansstate libfoo
bitbake libfoo

# Set PREFERRED_VERSION
PREFERRED_VERSION_libfoo = "2.0"
```

**sstate and Staging:**

```bash
# Staging can be restored from sstate-cache
# If sstate valid, do_populate_sysroot skipped
# Files extracted from sstate directly to sysroot

# sstate file contains staging manifest
sstate-cache/XX/sstate:libfoo:cortexa57:1.0:r0:cortexa57:3:HASH_populate_sysroot.tgz
```

**Real-World Example - CUDA:**

```bitbake
# cuda-toolkit recipe
do_install() {
    # Install CUDA headers and libraries
    install -d ${D}${prefix}/local/cuda/include
    cp -r ${S}/include/* ${D}${prefix}/local/cuda/include/

    install -d ${D}${prefix}/local/cuda/lib64
    cp -r ${S}/lib64/* ${D}${prefix}/local/cuda/lib64/
}

# Automatically staged to:
# ${STAGING_DIR_TARGET}/usr/local/cuda/

# Dependent recipe
DEPENDS = "cuda-toolkit"

do_configure() {
    # CMake finds CUDA via staged files
    cmake ${S} -DCUDA_TOOLKIT_ROOT_DIR=${STAGING_DIR_TARGET}/usr/local/cuda
}
```

**Key Points to Cover:**
- do_populate_sysroot stages development files
- Essential for cross-compilation
- Separate sysroots for target, native, cross
- Automatic dependency management
- sstate caches staged files
- Staging happens after do_install
- Only development files staged (headers, libs, pc files)
- Runtime files go to packages, not sysroot

**Follow-up Questions:**
1. What's the difference between ${D} and ${STAGING_DIR}?
2. How would you debug a recipe that can't find a dependency's header file?

**Red Flags (Weak Answers):**
- Not understanding difference between install and staging
- Confusing sysroot with target filesystem
- Not knowing staging is automatic
- Not understanding dependency triggers staging
- Never looked in sysroot directories
- Not knowing about sstate and staging relationship

---

## Summary

These 15 questions cover advanced recipe development for Mid to Senior-level positions:

- Patching and source management
- PACKAGECONFIG for flexible configuration
- Version management and PREFERRED_VERSION
- Directory variables (WORKDIR, S, B, D)
- devtool workflow
- Debugging build failures
- Override mechanism
- License management
- Classes and inheritance
- Custom image creation
- devshell for debugging
- Build optimization
- SRCPV and git versioning
- Cross-layer dependencies
- Sysroot and staging

Candidates should demonstrate practical experience with real-world recipe development scenarios.
