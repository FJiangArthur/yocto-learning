# Build System Fundamentals - Interview Questions

## Overview
This section covers fundamental Yocto/OpenEmbedded build system concepts suitable for Junior to Mid-level positions. These questions assess understanding of core BitBake concepts, build workflows, and basic recipe syntax.

---

### Q1: What is BitBake and how does it differ from Make? [Difficulty: Junior]

**Question:**
Explain what BitBake is and describe the key differences between BitBake and traditional Make-based build systems like GNU Make.

**Expected Answer:**
BitBake is a task execution engine and scheduler that forms the core of the Yocto Project build system. Unlike Make, which is primarily concerned with compiling source code into binaries:

1. **Task-based vs. File-based**: BitBake operates on tasks (do_fetch, do_compile, do_install) while Make operates on file dependencies
2. **Metadata-driven**: BitBake uses recipes (.bb files) that describe how to build software packages, including dependencies, source locations, and build instructions
3. **Dependency Resolution**: BitBake builds a complete dependency graph before execution and can parallelize independent tasks
4. **Cross-compilation Focus**: BitBake is designed for cross-compilation with built-in support for multiple architectures
5. **Package Management**: BitBake integrates with package management systems (RPM, DEB, IPK) to create complete Linux distributions

**Key Points to Cover:**
- BitBake is a Python-based build tool
- It reads recipes and configuration files to determine what to build
- Supports complex dependency resolution across multiple layers
- Can cache and reuse build artifacts (sstate-cache)

**Follow-up Questions:**
1. What is the purpose of the sstate-cache in BitBake?
2. How does BitBake determine task dependencies?

**Red Flags (Weak Answers):**
- Confusing BitBake with the entire Yocto Project
- Not understanding the task-based execution model
- Thinking BitBake is just a wrapper around Make

---

### Q2: Explain the directory structure of a typical Yocto build environment [Difficulty: Junior]

**Question:**
After running `source oe-init-build-env`, what are the key directories in a Yocto build environment and what is their purpose?

**Expected Answer:**
A typical Yocto build environment contains:

```
build/
├── conf/
│   ├── local.conf          # Local build configuration
│   ├── bblayers.conf       # Layer configuration
│   └── templateconf.cfg    # Template configuration
├── tmp/
│   ├── deploy/             # Final build artifacts
│   │   ├── images/         # Bootable images
│   │   ├── rpm/            # RPM packages
│   │   ├── ipk/            # IPK packages
│   │   └── licenses/       # License manifests
│   ├── work/               # Working directories for recipes
│   ├── sysroots/           # Cross-compilation sysroots
│   ├── stamps/             # Task completion markers
│   └── log/                # Build logs
├── downloads/              # Downloaded source code
└── sstate-cache/           # Shared state cache
```

**Key Points to Cover:**
- `conf/local.conf`: Machine type, distro features, parallelization settings
- `tmp/work/`: Contains per-recipe build directories with architecture-specific subdirectories
- `tmp/deploy/images/`: Final bootable images and kernel files
- `downloads/`: Shared across builds to avoid re-downloading
- `sstate-cache/`: Enables incremental builds and sharing between build servers

**Follow-up Questions:**
1. What happens if you delete the tmp/ directory?
2. Where would you find the log file for a failed recipe build?

**Red Flags (Weak Answers):**
- Not knowing the difference between tmp/deploy and tmp/work
- Not understanding the importance of sstate-cache
- Confusing downloads/ with source code locations

---

### Q3: What are the primary tasks in a BitBake recipe execution? [Difficulty: Junior]

**Question:**
Describe the standard task execution flow when BitBake builds a recipe, from source acquisition to package creation.

**Expected Answer:**
The primary tasks in order of execution:

1. **do_fetch**: Downloads source code from SCM, tarball URLs, or local files
2. **do_unpack**: Extracts source archives into the work directory
3. **do_patch**: Applies patches specified in SRC_URI
4. **do_configure**: Runs configuration scripts (./configure, cmake, etc.)
5. **do_compile**: Builds the software (make, ninja, etc.)
6. **do_install**: Installs files to a staging directory (${D})
7. **do_package**: Splits installed files into packages
8. **do_package_write_***: Creates package files (RPM, DEB, IPK)

Additional important tasks:
- **do_populate_sysroot**: Stages files for other recipes to use
- **do_rootfs**: Creates filesystem images (for image recipes)

**Key Points to Cover:**
- Tasks have dependencies (do_compile depends on do_configure)
- You can run individual tasks: `bitbake -c compile recipe-name`
- Tasks can be overridden or appended in recipes
- Some tasks are automatically created by inherit classes

**Follow-up Questions:**
1. How would you run only the configure task for debugging?
2. What's the difference between do_install and do_package?

**Red Flags (Weak Answers):**
- Not understanding the task dependency chain
- Confusing ${S} (source dir) with ${D} (destination/install dir)
- Not knowing you can execute tasks individually

---

### Q4: Explain SRC_URI and its components [Difficulty: Mid]

**Question:**
What is SRC_URI in a BitBake recipe, and what are the different types of URIs you can specify? Provide examples.

**Expected Answer:**
SRC_URI is a variable that specifies where to fetch source code and additional files (patches, configuration files) for a recipe.

Common URI schemes:

```bitbake
# Git repository
SRC_URI = "git://github.com/example/project.git;protocol=https;branch=master"

# HTTP/HTTPS tarball
SRC_URI = "https://example.com/source-1.0.tar.gz"

# FTP source
SRC_URI = "ftp://ftp.example.com/pub/source-1.0.tar.bz2"

# Local file from layer
SRC_URI = "file://example.patch \
           file://custom-config.txt \
          "

# SVN repository
SRC_URI = "svn://svn.example.com/repo/trunk;module=project;protocol=http"

# Multiple sources
SRC_URI = "https://example.com/source-${PV}.tar.gz \
           file://0001-fix-compilation.patch \
           file://init-script \
          "
```

URI parameters:
- `protocol`: Specify git/http/https/ssh
- `branch`: Git branch to checkout
- `rev`/`tag`: Specific revision or tag
- `destsuffix`: Extract location
- `name`: For multiple sources
- `subdir`: Subdirectory to place file

**Key Points to Cover:**
- SRC_URI supports checksums via SRC_URI[md5sum] and SRC_URI[sha256sum]
- Patches in SRC_URI are automatically applied in do_patch
- Files are searched in FILESPATH (recipe directory, layer files/)
- Variables like ${PV} can be used in SRC_URI

**Follow-up Questions:**
1. How do you specify different sources for different architectures?
2. What happens if a file:// URI cannot be found?

**Red Flags (Weak Answers):**
- Not knowing about file:// for local files
- Not understanding URI parameters (;protocol=https, ;branch=)
- Not knowing about checksum verification

---

### Q5: What is the difference between DEPENDS and RDEPENDS? [Difficulty: Mid]

**Question:**
Explain the difference between DEPENDS and RDEPENDS in BitBake recipes. When would you use each?

**Expected Answer:**

**DEPENDS (Build-time dependencies):**
- Specifies recipes needed during compilation
- These packages must be built and staged before this recipe compiles
- Used for libraries, headers, build tools needed at compile time
- Example: If building a C program that links against libssl, you need `DEPENDS = "openssl"`

```bitbake
DEPENDS = "openssl zlib virtual/kernel"
```

**RDEPENDS (Runtime dependencies):**
- Specifies packages needed at runtime on the target system
- These packages will be installed on the final image if this package is included
- Package-specific: `RDEPENDS:${PN} = "package-name"`
- Example: A Python script needs Python interpreter at runtime

```bitbake
RDEPENDS:${PN} = "python3 python3-numpy bash"
```

**Key Differences:**

| Aspect | DEPENDS | RDEPENDS |
|--------|---------|----------|
| When | Build time | Runtime |
| Scope | Recipe level | Package level |
| Purpose | Compilation | Execution |
| Target | Affects build order | Affects image contents |

**Practical Example:**
```bitbake
DESCRIPTION = "Network monitoring tool"
DEPENDS = "libpcap openssl"        # Needed to compile
RDEPENDS:${PN} = "tcpdump bash"    # Needed to run
```

**Key Points to Cover:**
- DEPENDS affects build scheduling and sysroot population
- RDEPENDS affects package management and image creation
- You can have DEPENDS without RDEPENDS and vice versa
- Virtual dependencies (virtual/kernel, virtual/libc)

**Follow-up Questions:**
1. What happens if you forget to add a DEPENDS but the build still succeeds?
2. How do you specify version requirements in RDEPENDS?

**Red Flags (Weak Answers):**
- Using DEPENDS for runtime dependencies
- Not understanding the package-specific nature of RDEPENDS
- Confusing recipe names with package names

---

### Q6: Explain the concept of MACHINE in Yocto [Difficulty: Junior]

**Question:**
What is the MACHINE variable in Yocto, and how does it affect the build process? Where is it typically set?

**Expected Answer:**
MACHINE defines the target hardware platform for the build. It determines:

1. **Architecture and toolchain**: ARM, x86_64, MIPS, etc.
2. **Kernel configuration**: Device tree, kernel defconfig
3. **Bootloader settings**: U-Boot, GRUB configuration
4. **Hardware features**: GPU, display, connectivity
5. **Image formatting**: Flash layout, partition sizes

**Configuration:**
```bash
# Set in conf/local.conf
MACHINE = "jetson-nano-devkit"
MACHINE = "raspberrypi4"
MACHINE = "qemux86-64"
```

**Machine Configuration Files:**
Located in `meta-layer/conf/machine/*.conf`

```bitbake
# Example: conf/machine/jetson-nano-devkit.conf
require conf/machine/include/tegra210.inc

KERNEL_DEVICETREE = "tegra210-p3450-0000.dtb"
MACHINE_FEATURES = "usbhost usbgadget ext2 screen wifi bluetooth"
PREFERRED_PROVIDER_virtual/kernel = "linux-tegra"
SERIAL_CONSOLES = "115200;ttyS0"
```

**Key Points to Cover:**
- MACHINE is typically set in local.conf
- Machine configuration files define hardware-specific settings
- Different machines can share common includes
- Affects which recipes and kernel configs are used
- Determines the output image format and bootloader

**Follow-up Questions:**
1. How would you create a custom MACHINE configuration?
2. What is the relationship between MACHINE and TUNE_FEATURES?

**Red Flags (Weak Answers):**
- Confusing MACHINE with DISTRO
- Not knowing where machine configs are located
- Thinking MACHINE is just about CPU architecture

---

### Q7: What is the purpose of BBPATH and BBFILES? [Difficulty: Mid]

**Question:**
Explain what BBPATH and BBFILES are used for in the Yocto build system and how they relate to layer configuration.

**Expected Answer:**

**BBPATH:**
- Defines the search path for configuration files and classes
- Similar to PATH in Linux
- Used to find conf/bitbake.conf, classes/*.bbclass
- Set automatically based on layers in bblayers.conf

**BBFILES:**
- A space-separated list of recipe file patterns
- Tells BitBake which .bb and .bbappend files to parse
- Uses wildcards to match recipe locations

**Example from bblayers.conf:**
```bitbake
# bblayers.conf
BBPATH = "${TOPDIR}"
BBFILES ?= ""

BBLAYERS ?= " \
  /path/to/poky/meta \
  /path/to/poky/meta-poky \
  /path/to/poky/meta-yocto-bsp \
  /path/to/meta-openembedded/meta-oe \
  /path/to/meta-custom \
  "
```

**Layer's layer.conf:**
```bitbake
# meta-custom/conf/layer.conf
BBPATH .= ":${LAYERDIR}"

BBFILES += "${LAYERDIR}/recipes-*/*/*.bb \
            ${LAYERDIR}/recipes-*/*/*.bbappend"

BBFILE_COLLECTIONS += "custom-layer"
BBFILE_PATTERN_custom-layer = "^${LAYERDIR}/"
BBFILE_PRIORITY_custom-layer = "10"
```

**Key Points to Cover:**
- BBPATH is extended (.=) by each layer
- BBFILES is accumulated (+=) from all layers
- Wildcards in BBFILES determine which recipes are visible
- Layer priority affects which bbappend files are applied
- BBFILE_COLLECTIONS provides unique layer identification

**Follow-up Questions:**
1. What happens if two layers have the same recipe filename?
2. How does BBFILE_PRIORITY affect recipe selection?

**Red Flags (Weak Answers):**
- Confusing BBPATH with PATH
- Not understanding the wildcard patterns in BBFILES
- Not knowing that each layer contributes to BBFILES

---

### Q8: Explain the difference between := and ?= and += in BitBake [Difficulty: Junior]

**Question:**
BitBake supports several variable assignment operators. Explain the difference between `:=`, `?=`, `+=`, `=`, and `.=` with examples.

**Expected Answer:**

**Assignment Operators:**

1. **`=` (Assignment)**
   - Standard assignment, expanded at reference time (lazy evaluation)
   ```bitbake
   A = "foo"
   B = "${A} bar"
   A = "baz"
   # B evaluates to "baz bar"
   ```

2. **`:=` (Immediate Assignment)**
   - Expanded immediately at assignment time
   ```bitbake
   A = "foo"
   B := "${A} bar"
   A = "baz"
   # B is "foo bar" (doesn't change)
   ```

3. **`?=` (Conditional Assignment)**
   - Assigns only if variable is not already set
   ```bitbake
   A ?= "default"
   # A is only "default" if not previously set
   ```

4. **`+=` (Append with space)**
   - Adds value with space separator
   ```bitbake
   SRC_URI = "file://first.patch"
   SRC_URI += "file://second.patch"
   # SRC_URI = "file://first.patch file://second.patch"
   ```

5. **`.=` (Append without space)**
   - Directly concatenates
   ```bitbake
   BBPATH = "/path/one"
   BBPATH .= ":/path/two"
   # BBPATH = "/path/one:/path/two"
   ```

6. **`=+` (Prepend with space)**
   ```bitbake
   IMAGE_INSTALL = "package-b"
   IMAGE_INSTALL =+ "package-a"
   # IMAGE_INSTALL = "package-a package-b"
   ```

7. **`=.` (Prepend without space)**
   ```bitbake
   PREFIX = "suffix"
   PREFIX =. "pre"
   # PREFIX = "presuffix"
   ```

**Key Points to Cover:**
- Lazy vs. immediate evaluation is critical for variable ordering
- `?=` is commonly used for defaults that users can override
- `+=` and `.=` are most common for lists and paths
- Understanding evaluation timing prevents subtle bugs

**Follow-up Questions:**
1. When would you use := instead of = ?
2. What's the difference between VARIABLE += "value" and VARIABLE:append = " value"?

**Red Flags (Weak Answers):**
- Not understanding lazy evaluation of =
- Confusing += with :append
- Not knowing about conditional assignment (?=)

---

### Q9: What is the purpose of the do_install task and ${D}? [Difficulty: Junior]

**Question:**
Explain the role of the do_install task and the ${D} variable. Why is it important to install files to ${D} rather than directly to the root filesystem?

**Expected Answer:**

**do_install Task:**
The do_install task copies built files from the build directory to a staging installation directory, organizing them as they should appear on the target filesystem.

**${D} Variable:**
- Represents the destination directory (install staging area)
- Typically expands to something like: `tmp/work/architecture/recipe-name/version/image/`
- Files installed to ${D} are organized exactly as they'll appear on target

**Example:**
```bitbake
do_install() {
    # Install binary
    install -d ${D}${bindir}
    install -m 0755 ${B}/myapp ${D}${bindir}/myapp

    # Install library
    install -d ${D}${libdir}
    install -m 0644 ${B}/libmylib.so.1.0 ${D}${libdir}/

    # Install configuration
    install -d ${D}${sysconfdir}/myapp
    install -m 0644 ${WORKDIR}/myapp.conf ${D}${sysconfdir}/myapp/

    # Install systemd service
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/myapp.service ${D}${systemd_system_unitdir}/
}
```

**Why ${D} is Important:**

1. **Isolation**: Prevents contaminating the build host
2. **Packaging**: Package managers need staged files to create packages
3. **Multi-package recipes**: Different files can go to different packages
4. **Image assembly**: Rootfs is assembled from staged packages, not direct installs
5. **Reproducibility**: Clean separation between build and install

**Common Variables:**
```bitbake
${bindir}       → /usr/bin
${sbindir}      → /usr/sbin
${libdir}       → /usr/lib
${includedir}   → /usr/include
${sysconfdir}   → /etc
${datadir}      → /usr/share
${mandir}       → /usr/share/man
```

**Key Points to Cover:**
- Never hardcode paths like /usr/bin, always use ${bindir}
- Use install command with proper permissions (-m)
- Create directories with install -d before installing files
- ${D} is cleared at the start of do_install

**Follow-up Questions:**
1. What happens after do_install completes?
2. How would you install files to different packages in a multi-package recipe?

**Red Flags (Weak Answers):**
- Installing directly to / without ${D}
- Not understanding that ${D} is a staging area
- Using cp instead of install command
- Hardcoding absolute paths

---

### Q10: Explain the concept of PREFERRED_PROVIDER [Difficulty: Mid]

**Question:**
What is PREFERRED_PROVIDER and when would you use it? Provide an example scenario.

**Expected Answer:**

**PREFERRED_PROVIDER:**
Used to select which recipe should provide a virtual target when multiple recipes provide the same functionality.

**Virtual Targets:**
These are abstract dependency names that can be satisfied by different recipes:
- `virtual/kernel` - Kernel provider
- `virtual/bootloader` - Bootloader provider
- `virtual/libc` - C library (glibc, musl, etc.)
- `virtual/libgl` - OpenGL implementation

**Example Scenario:**
Multiple recipes can provide a kernel:
```bitbake
# In meta-tegra/recipes-kernel/linux/
linux-tegra_5.10.bb → PROVIDES = "virtual/kernel"

# In meta-custom/recipes-kernel/linux/
linux-custom_5.15.bb → PROVIDES = "virtual/kernel"
```

**Selecting Provider:**
```bitbake
# In conf/local.conf or machine config
PREFERRED_PROVIDER_virtual/kernel = "linux-tegra"
PREFERRED_PROVIDER_virtual/bootloader = "u-boot-tegra"
PREFERRED_PROVIDER_virtual/libgl = "mesa"
```

**Real-world Example - Jetson Platform:**
```bitbake
# conf/machine/jetson-nano-devkit.conf
PREFERRED_PROVIDER_virtual/kernel = "linux-tegra"
PREFERRED_PROVIDER_virtual/bootloader = "cboot-t21x"
PREFERRED_PROVIDER_virtual/egl = "libglvnd"
PREFERRED_PROVIDER_virtual/libgles1 = "libglvnd"
PREFERRED_PROVIDER_virtual/libgles2 = "libglvnd"
```

**Multiple Providers Example:**
```bitbake
# Multiple recipes provide JPEG library
PREFERRED_PROVIDER_jpeg = "libjpeg-turbo"  # Instead of jpeg
PREFERRED_PROVIDER_jpeg-native = "libjpeg-turbo-native"
```

**Key Points to Cover:**
- PREFERRED_PROVIDER resolves ambiguity when multiple recipes PROVIDES the same target
- Commonly set in machine configurations or distro configs
- Essential for BSPs where multiple kernel options exist
- Can be overridden in local.conf for testing

**Follow-up Questions:**
1. What happens if you don't set PREFERRED_PROVIDER when multiple providers exist?
2. How is PREFERRED_PROVIDER different from PREFERRED_VERSION?

**Red Flags (Weak Answers):**
- Not understanding virtual/ providers
- Confusing PREFERRED_PROVIDER with DEPENDS
- Not knowing common virtual targets

---

### Q11: What is the sstate-cache and why is it important? [Difficulty: Mid]

**Question:**
Explain what the shared state cache (sstate-cache) is, how it works, and why it's crucial for build performance.

**Expected Answer:**

**Shared State Cache (sstate-cache):**
A directory containing cached outputs of BitBake tasks that can be reused across builds, machines, and even different build servers.

**How It Works:**

1. **Task Signatures**: BitBake calculates checksums (signatures) for each task based on:
   - Recipe content
   - Input files
   - Dependencies
   - Configuration variables affecting the task

2. **Cache Storage**: When a task completes, outputs are packaged into sstate files:
   ```
   sstate-cache/
   ├── 00/
   ├── 01/
   ├── ...
   └── ff/
       └── sstate:recipe-name:architecture:version:task.tgz
   ```

3. **Cache Reuse**: On subsequent builds, if task signature matches, BitBake:
   - Skips task execution
   - Extracts cached outputs
   - Marks task as complete

**Performance Impact:**

```bitbake
# Without sstate-cache
bitbake core-image-minimal
# First build: ~2 hours

# With sstate-cache
bitbake -c cleansstate core-image-minimal
bitbake core-image-minimal
# Rebuild with warm cache: ~10 minutes
```

**Configuration:**

```bitbake
# conf/local.conf
SSTATE_DIR ?= "${TOPDIR}/sstate-cache"

# Shared network cache
SSTATE_DIR = "/mnt/shared-sstate"

# Multiple cache locations (search order)
SSTATE_MIRRORS ?= "\
    file://.* http://sstate-cache.example.com/PATH;downloadfilename=PATH \
    file://.* file:///local-mirror/sstate-cache/PATH \
"
```

**What Gets Cached:**
- Compiled binaries (do_compile)
- Installed files (do_install)
- Packaged files (do_package)
- Populated sysroots (do_populate_sysroot)
- Image artifacts

**Key Points to Cover:**
- Sstate enables incremental builds
- Can be shared across build machines
- Signatures ensure cache validity
- Massive time savings in CI/CD pipelines
- Can cache native, cross, and target builds

**Follow-up Questions:**
1. How would you set up a shared sstate-cache server for a team?
2. When would cached sstate become invalid?

**Red Flags (Weak Answers):**
- Thinking sstate-cache is just downloaded sources (that's downloads/)
- Not understanding signature-based invalidation
- Not knowing sstate can be shared across machines

---

### Q12: Explain package groups (IMAGE_INSTALL vs PACKAGE_INSTALL) [Difficulty: Mid]

**Question:**
What is the difference between IMAGE_INSTALL and PACKAGE_INSTALL? How do packagegroups work in Yocto?

**Expected Answer:**

**IMAGE_INSTALL:**
- High-level list of packages to install in the image
- Set by user in image recipes or local.conf
- Supports package groups and individual packages
- Gets resolved to PACKAGE_INSTALL during image construction

**PACKAGE_INSTALL:**
- Low-level, fully resolved list of all packages
- Automatically computed by BitBake
- Includes all dependencies (RDEPENDS)
- Not typically set by users

**Example:**
```bitbake
# In conf/local.conf
IMAGE_INSTALL:append = " vim openssh python3"

# In image recipe (core-image-custom.bb)
IMAGE_INSTALL = "\
    packagegroup-core-boot \
    packagegroup-core-ssh-openssh \
    ${CORE_IMAGE_EXTRA_INSTALL} \
    myapp \
"
```

**Package Groups:**
Recipes that group related packages together:

```bitbake
# recipes-core/packagegroups/packagegroup-myapp.bb
DESCRIPTION = "My application package group"
inherit packagegroup

RDEPENDS:${PN} = "\
    myapp \
    myapp-config \
    python3 \
    python3-numpy \
    python3-opencv \
"
```

**Usage of Package Groups:**
```bitbake
IMAGE_INSTALL:append = " packagegroup-myapp"
```

**Standard Package Groups:**
- `packagegroup-core-boot` - Essential boot packages
- `packagegroup-core-ssh-openssh` - SSH server
- `packagegroup-core-buildessential` - Development tools
- `packagegroup-core-x11` - X11 graphics

**Relationship:**
```
IMAGE_INSTALL (user defined)
    ↓
Resolution of package groups and dependencies
    ↓
PACKAGE_INSTALL (fully resolved list)
    ↓
Actual packages installed in rootfs
```

**Key Points to Cover:**
- Use IMAGE_INSTALL:append (not =) to add packages in local.conf
- Package groups simplify managing related packages
- PACKAGE_INSTALL includes all recursive dependencies
- Package groups inherit from packagegroup class

**Follow-up Questions:**
1. How would you create a custom package group for a product?
2. What's the advantage of using package groups vs listing individual packages?

**Red Flags (Weak Answers):**
- Using IMAGE_INSTALL = instead of append
- Not understanding package group resolution
- Confusing IMAGE_INSTALL with DEPENDS

---

### Q13: What is SRCREV and how does it work with Git repositories? [Difficulty: Mid]

**Question:**
Explain the purpose of SRCREV when using Git repositories as sources. What are the best practices for setting SRCREV?

**Expected Answer:**

**SRCREV:**
Specifies the exact Git commit to checkout when using git:// in SRC_URI. Essential for reproducible builds.

**Basic Usage:**
```bitbake
SRC_URI = "git://github.com/example/project.git;protocol=https;branch=master"
SRCREV = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0"
```

**SRCREV Options:**

1. **Specific Commit (Production):**
   ```bitbake
   SRCREV = "d4f6e8a2b1c3d5e7f9a0b2c4d6e8f0a1b3c5d7e9"
   ```

2. **Latest Commit (Development - NOT for production):**
   ```bitbake
   SRCREV = "${AUTOREV}"
   ```

3. **Git Tag:**
   ```bitbake
   SRC_URI = "git://github.com/example/project.git;protocol=https;tag=v1.2.3"
   # When using tag, SRCREV is often set to tag name or left out
   SRCREV = "${AUTOREV}"
   ```

4. **Multiple Git Repos:**
   ```bitbake
   SRC_URI = "git://github.com/example/main.git;protocol=https;branch=master;name=main \
              git://github.com/example/plugin.git;protocol=https;branch=stable;name=plugin \
             "
   SRCREV_main = "abc123def456"
   SRCREV_plugin = "789ghi012jkl"
   ```

**Best Practices:**

```bitbake
# GOOD - Production recipe
SRCREV = "d4f6e8a2b1c3d5e7f9a0b2c4d6e8f0a1b3c5d7e9"
PV = "1.2.3+git${SRCPV}"  # SRCPV includes shortened git hash

# ACCEPTABLE - Development
SRCREV = "${AUTOREV}"
# Add to local.conf: BB_SRCREV_POLICY = "clear"

# BAD - Never in production
SRCREV = "master"  # Don't use branch names
```

**Updating SRCREV:**
```bash
# Find latest commit
git ls-remote https://github.com/example/project.git master

# Update recipe
SRCREV = "new-commit-hash-here"
```

**Impact on Build:**
```bitbake
# Force refetch after SRCREV change
bitbake -c cleanall recipe-name
bitbake recipe-name
```

**Key Points to Cover:**
- SRCREV ensures build reproducibility
- ${AUTOREV} always fetches latest (breaks reproducibility)
- Use specific commits for production/release
- Multiple repos need named SRCREVs
- SRCPV in PV creates version with git hash

**Follow-up Questions:**
1. How would you automate updating SRCREV for multiple recipes?
2. What are the implications of using AUTOREV in a production environment?

**Red Flags (Weak Answers):**
- Using AUTOREV without understanding implications
- Not knowing how to find commit hashes
- Confusing SRCREV with PV

---

### Q14: Explain the FILES variable and package splitting [Difficulty: Mid]

**Question:**
What is the FILES variable used for? How does BitBake split a recipe into multiple packages, and how do you control which files go into which package?

**Expected Answer:**

**FILES Variable:**
Defines which installed files belong to each package created by a recipe. BitBake uses pattern matching to split do_install outputs into separate packages.

**Default Package Split:**

A single recipe can create multiple packages:
```bitbake
# Recipe: myapp_1.0.bb produces packages:
myapp                    # Main runtime package
myapp-dev                # Development files (headers, .la files)
myapp-dbg                # Debug symbols
myapp-staticdev          # Static libraries
myapp-doc                # Documentation
myapp-locale-*           # Translation files
```

**Default FILES Assignments:**
```bitbake
# Defined in bitbake.conf and package.bbclass
FILES:${PN} = "${bindir}/* ${sbindir}/* ${libexecdir}/* \
               ${libdir}/lib*.so.* \
               ${sysconfdir} ${sharedstatedir} ${localstatedir} \
               ${datadir}/${BPN} ${libdir}/${BPN}/* \
               ${datadir}/pixmaps ${datadir}/applications \
               ${datadir}/idl ${datadir}/omf ${datadir}/sounds \
               ${libdir}/bonobo/servers"

FILES:${PN}-dev = "${includedir} ${FILES_SOLIBSDEV} ${libdir}/*.la \
                   ${libdir}/*.o ${libdir}/pkgconfig ${datadir}/pkgconfig \
                   ${datadir}/aclocal ${base_libdir}/*.o \
                   ${libdir}/${BPN}/*.la ${base_libdir}/*.la"

FILES:${PN}-dbg = "/usr/lib/debug /usr/src/debug"

FILES:${PN}-staticdev = "${libdir}/*.a ${base_libdir}/*.a"

FILES:${PN}-doc = "${docdir} ${mandir} ${infodir} ${datadir}/gtk-doc"
```

**Custom Package Splitting:**

```bitbake
DESCRIPTION = "My application with plugins"

# Define additional packages
PACKAGES =+ "${PN}-plugin-foo ${PN}-plugin-bar ${PN}-tools"

# Main application
FILES:${PN} = "${bindir}/myapp \
               ${libdir}/libmyapp.so.* \
               ${sysconfdir}/myapp/myapp.conf \
              "

# Plugins as separate packages
FILES:${PN}-plugin-foo = "${libdir}/myapp/plugins/foo.so"
FILES:${PN}-plugin-bar = "${libdir}/myapp/plugins/bar.so"

# Utility tools
FILES:${PN}-tools = "${bindir}/myapp-tool \
                     ${bindir}/myapp-debug \
                    "

# Dependencies
RDEPENDS:${PN} = "bash"
RDEPENDS:${PN}-plugin-foo = "${PN} libfoo"
RDEPENDS:${PN}-plugin-bar = "${PN} libbar"
```

**Package Ordering:**
```bitbake
# Order matters! More specific packages first
PACKAGES = "${PN}-plugin-foo ${PN}-plugin-bar ${PN}-tools \
            ${PN}-dbg ${PN}-dev ${PN}-staticdev ${PN}-doc \
            ${PN}"
```

**Practical Example:**
```bitbake
do_install() {
    # Main binary
    install -d ${D}${bindir}
    install -m 0755 ${B}/myapp ${D}${bindir}/

    # Plugins
    install -d ${D}${libdir}/myapp/plugins
    install -m 0755 ${B}/plugins/foo.so ${D}${libdir}/myapp/plugins/
    install -m 0755 ${B}/plugins/bar.so ${D}${libdir}/myapp/plugins/

    # Development headers
    install -d ${D}${includedir}/myapp
    install -m 0644 ${S}/include/*.h ${D}${includedir}/myapp/
}

# These files are automatically split by FILES assignments above
```

**Key Points to Cover:**
- One recipe can produce multiple packages
- PACKAGES defines which packages are created
- FILES:${PN} controls file assignment
- Use PACKAGES =+ to add packages before ${PN}
- Package order matters (first match wins)
- Each package can have its own RDEPENDS

**Follow-up Questions:**
1. Why would you want to split a recipe into multiple packages?
2. What happens if a file doesn't match any FILES pattern?

**Red Flags (Weak Answers):**
- Not understanding that one recipe creates multiple packages
- Confusing FILES with SRC_URI
- Not knowing about -dev, -dbg, -doc automatic splitting
- Not understanding PACKAGES ordering

---

### Q15: What is the difference between native, nativesdk, and cross recipes? [Difficulty: Mid]

**Question:**
Explain the different recipe types in Yocto (native, nativesdk, cross) and when each is used. Provide examples.

**Expected Answer:**

**Recipe Types Based on Execution Context:**

**1. Native Recipes (runs on build host):**
- Built and executed on the build machine
- Used for build tools needed during compilation
- Suffix: `-native`

```bitbake
# cmake-native.bb - CMake that runs on build host
DEPENDS = "cmake-native"

# Example: Building requires cmake on build machine
do_configure() {
    cmake ${S}
}
```

**2. Target Recipes (runs on target device):**
- Default type, no special suffix
- Cross-compiled for target architecture
- Runs on embedded device

```bitbake
# opencv_4.6.0.bb - OpenCV for target
# Will be cross-compiled for ARM/x86/etc.
```

**3. Cross Recipes (cross-compiler tools):**
- Compiler toolchain that runs on build host to produce target binaries
- Automatically created by Yocto
- Example: `gcc-cross`, `binutils-cross`

**4. NativeSDK Recipes (runs on SDK host):**
- Part of SDK that runs on developer's machine
- Used in extensible SDK
- Suffix: `-nativesdk`

```bitbake
# nativesdk-cmake.bb - CMake for SDK
# Runs on developer workstation, not build server
```

**Comparison Table:**

| Type | Runs On | Compiled For | Example | Use Case |
|------|---------|--------------|---------|----------|
| native | Build host | Build host | cmake-native | Build tools |
| target | Target device | Target arch | opencv | Application |
| cross | Build host | Produces target code | gcc-cross | Compiler |
| nativesdk | SDK host | SDK host | nativesdk-gcc | SDK tools |

**Practical Example:**

```bitbake
DESCRIPTION = "Example showing different recipe types"

# Native dependency - tool runs during build
DEPENDS = "protobuf-native"

# Target dependency - library for target
DEPENDS += "openssl zlib"

do_compile() {
    # protoc from protobuf-native runs on build host
    protoc --cpp_out=${B} ${S}/messages.proto

    # Cross-compiler (gcc-cross) compiles for target
    ${CC} -o myapp main.cpp -lssl -lz
}
```

**Creating Native Variants:**

```bitbake
# Option 1: Inherit native
# recipe-native.bb
require recipe.inc
inherit native

# Option 2: BBCLASSEXTEND
# recipe.bb
BBCLASSEXTEND = "native nativesdk"
# This creates: recipe-native.bb and nativesdk-recipe.bb automatically
```

**Common Native Tools:**
- cmake-native
- python3-native
- protobuf-native
- pkgconfig-native
- autoconf-native

**Key Points to Cover:**
- Native tools must be explicitly specified with -native suffix
- Cross-compilers are automatically configured
- BBCLASSEXTEND can create native variants automatically
- Understanding prevents "command not found" build errors
- SDK recipes are for extensible SDK distribution

**Follow-up Questions:**
1. When would you need to create a -native version of a recipe?
2. How does BitBake know which compiler to use for cross-compilation?

**Red Flags (Weak Answers):**
- Not understanding why -native is needed
- Confusing native with cross
- Not knowing about BBCLASSEXTEND
- Thinking all dependencies are automatically native

---

## Summary

These 15 questions cover fundamental Yocto/BitBake concepts essential for Junior to Mid-level embedded Linux positions. They test understanding of:

- Build system architecture and task execution
- Recipe syntax and variable usage
- Dependency management
- Package creation and splitting
- Configuration and machine definitions
- Source management and versioning
- Build optimization (sstate-cache)

Candidates should be able to explain these concepts clearly and provide practical examples from real-world scenarios.
