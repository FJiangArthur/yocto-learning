# Lab 01: First Yocto Build - Step-by-Step Instructions

## Introduction

This guide will walk you through building your first Yocto Project image from scratch. Follow each step carefully and read the explanations to understand what you're doing.

**Important**: Do not skip steps, even if they seem trivial. Each step builds on the previous one.

---

## Task 1: Prepare Your Build Host (15 minutes)

### Step 1.1: Update Your System

```bash
sudo apt update
sudo apt upgrade -y
```

**Explanation:** Ensures your package manager has the latest package information and all system packages are up to date. This prevents version conflicts with Yocto dependencies.

**Expected Output:**
```
Reading package lists... Done
Building dependency tree... Done
...
0 upgraded, 0 newly installed, 0 to remove and 0 not upgraded.
```

### Step 1.2: Install Required Build Dependencies

```bash
sudo apt install -y gawk wget git diffstat unzip texinfo gcc build-essential \
chrpath socat cpio python3 python3-pip python3-pexpect xz-utils debianutils \
iputils-ping python3-git python3-jinja2 python3-subunit zstd liblz4-tool file \
locales libacl1
```

**Explanation:** These packages are required by the Yocto Project build system. They include compilers, build tools, Python modules, and utilities that BitBake uses during the build process.

**Expected Output:**
```
Reading package lists... Done
Building dependency tree... Done
The following NEW packages will be installed:
  build-essential chrpath cpio debianutils diffstat gawk git ...
...
Processing triggers for libc-bin (2.35-0ubuntu3.1) ...
```

### Step 1.3: Verify Git Configuration

```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
git config --list | grep user
```

**Explanation:** Git configuration is required for some Yocto operations that track changes. Replace with your actual name and email.

**Expected Output:**
```
user.name=Your Name
user.email=your.email@example.com
```

### Step 1.4: Set Locale (Important for BitBake)

```bash
sudo locale-gen en_US.UTF-8
export LC_ALL=en_US.UTF-8
echo 'export LC_ALL=en_US.UTF-8' >> ~/.bashrc
```

**Explanation:** BitBake requires UTF-8 locale. Without this, you may encounter build errors related to character encoding.

**Expected Output:**
```
Generating locales (this might take a moment)...
  en_US.UTF-8... done
Generation complete.
```

## Checkpoint 1
Before proceeding, verify:
- [ ] All packages installed without errors
- [ ] Git is configured with your name and email
- [ ] Locale is set to en_US.UTF-8

---

## Task 2: Set Up Build Directory Structure (10 minutes)

### Step 2.1: Create Workspace Directory

```bash
mkdir -p ~/yocto-labs/lab-01
cd ~/yocto-labs/lab-01
pwd
```

**Explanation:** Creates a dedicated workspace for this lab. Keeping builds organized in separate directories makes management easier.

**Expected Output:**
```
/home/username/yocto-labs/lab-01
```

### Step 2.2: Clone Poky (Yocto Reference Distribution)

```bash
git clone -b kirkstone git://git.yoctoproject.org/poky.git
cd poky
git log -1 --oneline
```

**Explanation:** Clones the Poky reference distribution at the "kirkstone" branch (LTS release). Poky includes BitBake, metadata, and core recipes.

**Expected Output:**
```
Cloning into 'poky'...
remote: Counting objects: 500000, done.
...
Resolving deltas: 100% (400000/400000), done.
a1b2c3d4e (HEAD -> kirkstone) poky: update version to 4.0.x
```

**Note:** Clone time depends on your internet connection. This may take 5-15 minutes.

### Step 2.3: Verify Poky Structure

```bash
ls -la
ls meta/
```

**Explanation:** Poky contains several meta-layers. The most important are meta (core), meta-poky (distro config), and meta-yocto-bsp (board support).

**Expected Output:**
```
drwxrwxr-x  7 user user   4096 ... bitbake
drwxrwxr-x 19 user user   4096 ... meta
drwxrwxr-x  8 user user   4096 ... meta-poky
drwxrwxr-x  9 user user   4096 ... meta-yocto-bsp
...
```

## Checkpoint 2
Before proceeding, verify:
- [ ] You are in ~/yocto-labs/lab-01/poky directory
- [ ] Git clone completed successfully
- [ ] meta, meta-poky, and meta-yocto-bsp directories exist

---

## Task 3: Initialize Build Environment (10 minutes)

### Step 3.1: Source the Build Environment Script

```bash
source oe-init-build-env build-minimal
```

**Explanation:** This script initializes the build environment by:
- Setting up required environment variables
- Creating a build directory (build-minimal)
- Generating default configuration files
- Changing your current directory to the build directory

**Expected Output:**
```
You had no conf/local.conf file. This configuration file has therefore been
created for you from .../meta-poky/conf/local.conf.sample
You may wish to edit it to, for example, select a different MACHINE (target
hardware).

You had no conf/bblayers.conf file. This configuration file has therefore been
created for you from .../meta-poky/conf/bblayers.conf.sample

### Shell environment set up for builds. ###

You can now run 'bitbake <target>'

Common targets are:
    core-image-minimal
    core-image-full-cmdline
    core-image-sato
    core-image-weston
```

### Step 3.2: Verify Your Location

```bash
pwd
echo $BUILDDIR
```

**Explanation:** After sourcing the environment, you should be in the build directory with BUILDDIR variable set.

**Expected Output:**
```
/home/username/yocto-labs/lab-01/poky/build-minimal
/home/username/yocto-labs/lab-01/poky/build-minimal
```

### Step 3.3: Examine Configuration Files

```bash
ls -la conf/
cat conf/bblayers.conf
```

**Explanation:** The conf/ directory contains your build configuration. We'll modify these files next.

**Expected Output:**
```
-rw-rw-r-- 1 user user  386 ... bblayers.conf
-rw-rw-r-- 1 user user 2906 ... local.conf
-rw-rw-r-- 1 user user  115 ... templateconf.cfg
```

## Checkpoint 3
Before proceeding, verify:
- [ ] Current directory is build-minimal
- [ ] BUILDDIR environment variable is set
- [ ] conf/local.conf and conf/bblayers.conf exist

---

## Task 4: Configure the Build (15 minutes)

### Step 4.1: Edit local.conf - Set Machine

```bash
# Backup original configuration
cp conf/local.conf conf/local.conf.backup

# Edit local.conf
vim conf/local.conf
# OR
nano conf/local.conf
```

Find the line:
```
#MACHINE ?= "qemuarm"
```

Change it to:
```
MACHINE ?= "qemux86-64"
```

**Explanation:** MACHINE defines the target hardware. qemux86-64 is a 64-bit x86 emulator - perfect for testing without physical hardware.

### Step 4.2: Edit local.conf - Optimize Build Settings

Add these lines to the end of conf/local.conf:

```bash
# Parallel build optimization
BB_NUMBER_THREADS ?= "8"
PARALLEL_MAKE ?= "-j 8"

# Download directory (cache for source files)
DL_DIR ?= "${TOPDIR}/../downloads"

# Shared state cache (speeds up rebuilds)
SSTATE_DIR ?= "${TOPDIR}/../sstate-cache"

# Disk space monitoring
BB_DISKMON_DIRS = "\
    STOPTASKS,${TMPDIR},1G,100K \
    STOPTASKS,${DL_DIR},1G,100K \
    STOPTASKS,${SSTATE_DIR},1G,100K \
    STOPTASKS,/tmp,100M,100K \
    HALT,${TMPDIR},100M,1K \
    HALT,${DL_DIR},100M,1K \
    HALT,${SSTATE_DIR},100M,1K \
    HALT,/tmp,10M,1K"

# Package management
PACKAGE_CLASSES ?= "package_rpm"

# Additional free space for root filesystem
IMAGE_ROOTFS_EXTRA_SPACE = "524288"

# Remove old images to save space
RM_OLD_IMAGE = "1"
```

**Explanation:**
- **BB_NUMBER_THREADS**: Number of parallel BitBake tasks (adjust based on CPU cores)
- **PARALLEL_MAKE**: Number of parallel compile jobs (adjust based on CPU cores)
- **DL_DIR**: Shared download directory (reusable across builds)
- **SSTATE_DIR**: Shared state cache (dramatically speeds up rebuilds)
- **BB_DISKMON_DIRS**: Prevents build failures due to low disk space
- **PACKAGE_CLASSES**: Use RPM for package management
- **IMAGE_ROOTFS_EXTRA_SPACE**: Adds 512MB extra space to root filesystem

**Optimization Tip:** Set BB_NUMBER_THREADS and PARALLEL_MAKE to your CPU core count. Find your cores with:
```bash
nproc
```

### Step 4.3: Verify local.conf Changes

```bash
grep "^MACHINE" conf/local.conf
grep "^BB_NUMBER_THREADS" conf/local.conf
grep "^DL_DIR" conf/local.conf
```

**Expected Output:**
```
MACHINE ?= "qemux86-64"
BB_NUMBER_THREADS ?= "8"
DL_DIR ?= "${TOPDIR}/../downloads"
```

### Step 4.4: Examine bblayers.conf

```bash
cat conf/bblayers.conf
```

**Explanation:** bblayers.conf lists all metadata layers included in your build. For this minimal build, we only need the default Poky layers.

**Expected Output:**
```
BBLAYERS ?= " \
  /home/username/yocto-labs/lab-01/poky/meta \
  /home/username/yocto-labs/lab-01/poky/meta-poky \
  /home/username/yocto-labs/lab-01/poky/meta-yocto-bsp \
  "
```

**Note:** No changes needed for bblayers.conf in this lab. We'll modify this in Lab 03.

## Checkpoint 4
Before proceeding, verify:
- [ ] MACHINE is set to qemux86-64
- [ ] BB_NUMBER_THREADS and PARALLEL_MAKE are configured
- [ ] DL_DIR and SSTATE_DIR are set
- [ ] You have backed up local.conf

---

## Task 5: Start the Build (90-180 minutes)

### Step 5.1: Understand What Will Happen

Before starting, understand that BitBake will:
1. Parse all recipes and metadata
2. Download source code for ~3000 packages
3. Compile toolchain (cross-compiler for target)
4. Compile packages for target architecture
5. Create root filesystem
6. Build kernel and bootloader
7. Create bootable image

**Download size:** 5-10 GB
**Disk space used:** 40-60 GB
**Time:** 1.5-3 hours (first build)

### Step 5.2: Start the Build

```bash
bitbake core-image-minimal
```

**Explanation:** Starts building the minimal image recipe. Monitor the output carefully.

**Expected Output (initial):**
```
Parsing recipes: 100% |####################| Time: 0:02:34
Parsing of 862 .bb files complete (0 cached, 862 parsed). 1334 targets, 42 skipped, 0 masked, 0 errors.
NOTE: Resolving any missing task queue dependencies
Initialising tasks: 100% |##################| Time: 0:00:03
Checking sstate mirror object availability: 100% |##########| Time: 0:00:05
Sstate summary: Wanted 1204 Local 0 Network 0 Missed 1204 Current 0 (0% match, 0% complete)
NOTE: Executing Tasks
Currently  5 running tasks (2340 of 3214)  72% |##############      |
0: gcc-cross-x86_64-11.3.0-r0 do_compile - 00:05:23 (pid 12345)
1: binutils-cross-x86_64-2.38-r0 do_compile - 00:03:12 (pid 12346)
...
```

### Step 5.3: Monitor Build Progress

The build will run for several hours. You can:

**Check current status:**
```bash
# In another terminal, source environment first
cd ~/yocto-labs/lab-01/poky
source oe-init-build-env build-minimal

# Then check progress
bitbake core-image-minimal --status
```

**Monitor disk usage:**
```bash
df -h ~/yocto-labs/lab-01/
```

**View running tasks:**
```bash
tail -f tmp/log/cooker/qemux86-64/console-latest.log
```

### Step 5.4: Understanding Build Output

You'll see three types of messages:

**INFO Messages (NOTE:)** - Normal operation
```
NOTE: Executing task do_compile for recipe linux-yocto
```

**WARNING Messages** - Non-fatal issues
```
WARNING: QA Issue: package contains .la files [installed-vs-shipped]
```

**ERROR Messages** - Build failures
```
ERROR: Task do_compile failed with exit code '1'
```

**Explanation:**
- NOTE: Just informational, everything is fine
- WARNING: Should investigate but won't stop build
- ERROR: Build failed, must fix before continuing

### Step 5.5: What to Do While Building

The first build takes hours. During this time:
1. Read TROUBLESHOOTING.md to understand common errors
2. Read VERIFICATION.md to understand success criteria
3. Review Yocto documentation
4. Do NOT close terminal or interrupt build
5. Ensure system doesn't go to sleep

**Tip:** Use `screen` or `tmux` for long-running builds:
```bash
sudo apt install screen
screen -S yocto-build
# Start build here
# Press Ctrl+A then D to detach
# screen -r yocto-build to reattach
```

## Checkpoint 5
During build, monitor for:
- [ ] No ERROR messages
- [ ] Disk space not running out
- [ ] Build progress increasing
- [ ] No repeated task failures

---

## Task 6: Verify Build Success (10 minutes)

### Step 6.1: Check Build Completion

**Expected Final Output:**
```
NOTE: Tasks Summary: Attempted 3214 tasks of which 0 didn't need to be rerun and all succeeded.
```

**If you see this, your build succeeded!**

### Step 6.2: Locate Build Artifacts

```bash
cd tmp/deploy/images/qemux86-64/
ls -lh
```

**Expected Output:**
```
-rw-r--r-- 2 user user  9.1M ... bzImage
-rw-r--r-- 2 user user  6.5M ... core-image-minimal-qemux86-64.ext4
-rw-r--r-- 2 user user  3.2M ... core-image-minimal-qemux86-64.tar.bz2
-rw-r--r-- 1 user user  149K ... core-image-minimal-qemux86-64.testdata.json
...
```

**Key Files:**
- **bzImage**: Linux kernel
- **core-image-minimal-qemux86-64.ext4**: Root filesystem (ext4 format)
- **core-image-minimal-qemux86-64.tar.bz2**: Root filesystem (compressed archive)

### Step 6.3: Check Image Size

```bash
ls -lh core-image-minimal-qemux86-64.ext4
du -sh ../../
```

**Expected Output:**
```
-rw-r--r-- 2 user user 6.5M ... core-image-minimal-qemux86-64.ext4
45G     ../../
```

### Step 6.4: Run Verification Script

```bash
cd ~/yocto-labs/lab-01/poky/build-minimal
bash /path/to/lab-01-first-build/scripts/verify.sh
```

**Expected Output:**
```
====================================
Lab 01: Build Verification
====================================
[PASS] Build directory exists
[PASS] Configuration files present
[PASS] Build completed successfully
[PASS] Kernel image found
[PASS] Root filesystem found
[PASS] Build artifacts verified

All checks passed! ✓
```

## Checkpoint 6
Final verification:
- [ ] Build completed with "all succeeded"
- [ ] Kernel (bzImage) exists
- [ ] Root filesystem (*.ext4) exists
- [ ] verify.sh script passes all checks
- [ ] Build used ~40-60GB disk space

---

## Task 7: Explore Build Outputs (15 minutes)

### Step 7.1: Understand tmp/ Directory Structure

```bash
cd ~/yocto-labs/lab-01/poky/build-minimal
tree -L 2 tmp/ | head -30
```

**Directory Structure:**
```
tmp/
├── deploy/          # Final build outputs
│   ├── images/      # Bootable images
│   ├── rpm/         # Built packages
│   └── licenses/    # License information
├── work/            # Per-recipe build work
│   ├── core2-64-poky-linux/  # Target architecture
│   └── x86_64-linux/          # Build host
├── log/             # Build logs
└── sysroots-components/  # Sysroot files
```

### Step 7.2: Examine Package List

```bash
ls tmp/deploy/rpm/core2_64/ | head -20
```

**Explanation:** Each recipe produces RPM packages. These are the building blocks of your image.

**Expected Output:**
```
bash-5.1.16-r0.core2_64.rpm
busybox-1.35.0-r0.core2_64.rpm
glibc-2.35-r0.core2_64.rpm
...
```

### Step 7.3: Check Build Statistics

```bash
cat tmp/buildstats/*/core-image-minimal/build_stats
```

**Explanation:** Build statistics show how long each task took. Useful for optimization.

### Step 7.4: View Installed Packages

```bash
cat tmp/deploy/images/qemux86-64/core-image-minimal-qemux86-64.rootfs.manifest
```

**Explanation:** Manifest lists all packages in the final image. Critical for security audits.

**Expected Output:**
```
base-files 3.0.14
busybox 1.35.0
glibc 2.35
init-ifupdown 1.0
...
```

### Step 7.5: (Optional) Boot Image in QEMU

```bash
runqemu qemux86-64 nographic
```

**Explanation:** Tests your image in QEMU emulator. Login as root (no password).

**Expected Output:**
```
Poky (Yocto Project Reference Distro) 4.0 qemux86-64 ttyS0

qemux86-64 login: root
root@qemux86-64:~# uname -a
Linux qemux86-64 5.15.32-yocto-standard #1 SMP PREEMPT x86_64 GNU/Linux
root@qemux86-64:~# exit
```

**To exit QEMU:** Type `poweroff` or press Ctrl+A then X

---

## Summary

**Congratulations!** You have successfully:
- ✓ Set up a Yocto build environment
- ✓ Configured build settings
- ✓ Built a complete Linux system from source
- ✓ Generated bootable images
- ✓ Verified build artifacts

**What You Built:**
- Linux kernel: 9.1 MB
- Root filesystem: 6.5 MB
- Total: A complete bootable Linux system in under 10 MB!

**Build Statistics:**
- Recipes parsed: ~860
- Tasks executed: ~3200
- Packages built: ~130
- Time: 1.5-3 hours

---

## Next Steps

1. **Clean up** (optional): Remove tmp/ to free 40GB
   ```bash
   bitbake -c cleanall core-image-minimal
   ```

2. **Practice rebuild**: Build again (will be much faster!)
   ```bash
   bitbake core-image-minimal
   ```

3. **Proceed to Lab 02**: Custom Recipe Development

4. **Experiment**:
   - Try different MACHINE targets (qemuarm, qemuarm64)
   - Build core-image-base for more features
   - Explore recipes in meta/recipes-*/

---

## Troubleshooting

If you encountered errors, see **TROUBLESHOOTING.md** for solutions to common problems.

If build failed:
1. Read error message carefully
2. Check log files in tmp/log/
3. Search error message online
4. Refer to TROUBLESHOOTING.md

---

**Lab Complete!** Proceed to verification checklist in VERIFICATION.md.
