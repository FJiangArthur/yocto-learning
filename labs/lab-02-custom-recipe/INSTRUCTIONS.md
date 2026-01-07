# Lab 02: Custom Recipe Development - Instructions
## Step-by-Step Guide to Building Your First Recipe

This guide walks you through creating a complete BitBake recipe for a custom C application, from writing source code to integrating it into your Yocto image.

---

## Step 1: Create Recipe Directory Structure
**Time:** 10 minutes | **Goal:** Set up organized directories

```bash
cd ~/yocto-jetson/builds/jetson-orin-agx
source ~/yocto-jetson/poky/oe-init-build-env .

# Create custom layer if needed
cd ~/yocto-jetson/poky
bitbake-layers create-layer ../meta-custom

# Create recipe directory
mkdir -p ~/yocto-jetson/meta-custom/recipes-apps/hello-world/files
tree ~/yocto-jetson/meta-custom
```

**Checkpoint:**  Directory structure created

---

## Step 2: Create C Application
**Time:** 15 minutes | **Goal:** Write hello-world program

```bash
cd ~/yocto-jetson/meta-custom/recipes-apps/hello-world/files/

cat > hello-world.c << 'EOF'
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/utsname.h>

#define VERSION "1.0"

void print_banner(void) {
    printf("================================\n");
    printf("  Hello World - Yocto Edition  \n");
    printf("  Version: %s                  \n", VERSION);
    printf("================================\n\n");
}

void print_system_info(void) {
    struct utsname sys_info;
    if (uname(&sys_info) == 0) {
        printf("System Information:\n");
        printf("  OS Name:    %s\n", sys_info.sysname);
        printf("  Hostname:   %s\n", sys_info.nodename);
        printf("  Kernel:     %s\n", sys_info.release);
        printf("  Arch:       %s\n", sys_info.machine);
        printf("\n");
    }
}

int main(int argc, char *argv[]) {
    print_banner();
    print_system_info();
    printf("Built with Yocto on %s %s\n\n", __DATE__, __TIME__);
    return 0;
}
EOF
```

**Checkpoint:**  hello-world.c created

---

## Step 3: Create Makefile
**Time:** 10 minutes | **Goal:** Define build system

```bash
cat > Makefile << 'EOF'
CC ?= gcc
CFLAGS ?= -Wall -Wextra -O2
PREFIX ?= /usr
DESTDIR ?=
bindir = $(PREFIX)/bin

TARGET = hello-world
SRCS = hello-world.c

all: $(TARGET)

$(TARGET): $(SRCS)
	$(CC) $(CFLAGS) -o $@ $<

install: $(TARGET)
	install -d $(DESTDIR)$(bindir)
	install -m 0755 $(TARGET) $(DESTDIR)$(bindir)/

clean:
	rm -f $(TARGET)

.PHONY: all install clean
EOF
```

**Checkpoint:**  Makefile created

---

## Step 4: Create BitBake Recipe
**Time:** 20 minutes | **Goal:** Write complete recipe

```bash
cd ~/yocto-jetson/meta-custom/recipes-apps/hello-world/

cat > hello-world_1.0.bb << 'EOF'
SUMMARY = "Hello World demonstration application"
DESCRIPTION = "Simple app demonstrating Yocto recipe development"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://hello-world.c \
           file://Makefile \
          "

S = "${WORKDIR}"

do_compile() {
    oe_runmake
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 hello-world ${D}${bindir}/
}

FILES:${PN} = "${bindir}/hello-world"
EOF
```

**Key Variables Explained:**
- `SUMMARY`: One-line description
- `LICENSE`: Software license
- `SRC_URI`: Source files location
- `S`: Source directory (${WORKDIR} = unpacked sources)
- `${D}`: Destination rootfs staging area
- `${bindir}`: Expands to /usr/bin

**Checkpoint:**  Recipe file created

---

## Step 5: Add Layer to Build
**Time:** 5 minutes | **Goal:** Register custom layer

```bash
cd ~/yocto-jetson/builds/jetson-orin-agx
source ~/yocto-jetson/poky/oe-init-build-env .

bitbake-layers add-layer ~/yocto-jetson/meta-custom
bitbake-layers show-layers | grep meta-custom
```

**Checkpoint:**  meta-custom appears in layer list

---

## Step 6: Build the Recipe
**Time:** 10 minutes | **Goal:** Compile application

```bash
bitbake -p  # Parse check
bitbake hello-world
echo $?  # Should be 0
```

**Expected:** "all succeeded" message

**Checkpoint:**  Build completes successfully

---

## Step 7: Verify Build Artifacts
**Time:** 5 minutes | **Goal:** Check compiled binary

```bash
find tmp/work -name hello-world -type f -executable
file tmp/work/aarch64-oe-linux/hello-world/1.0-*/image/usr/bin/hello-world
```

**Expected:** "ARM aarch64" executable

**Checkpoint:**  Binary is correct architecture

---

## Step 8: Add to Image
**Time:** 5 minutes | **Goal:** Include in rootfs

```bash
echo 'IMAGE_INSTALL:append = " hello-world"' >> conf/local.conf
grep hello-world conf/local.conf
```

**Checkpoint:**  hello-world in IMAGE_INSTALL

---

## Step 9: Build Image
**Time:** 15 minutes | **Goal:** Build complete image

```bash
bitbake core-image-minimal
grep hello-world tmp/deploy/images/*/core-image-minimal*.manifest
```

**Expected:** hello-world listed in manifest

**Checkpoint:**  Package included in image

---

## Step 10: Test in QEMU
**Time:** 10 minutes | **Goal:** Verify application works

```bash
# Build for QEMU
sed -i 's/MACHINE.*/MACHINE = "qemuarm64"/' conf/local.conf
bitbake core-image-minimal

# Launch QEMU
runqemu qemuarm64 nographic

# Inside QEMU (login as root):
# hello-world
# which hello-world
# poweroff
```

**Expected Output:**
```
================================
  Hello World - Yocto Edition
  Version: 1.0
================================

System Information:
  OS Name:    Linux
  Hostname:   qemuarm64
  Kernel:     5.10.xxx
  Arch:       aarch64
```

**Checkpoint:**  Application runs in QEMU

---

## Debugging Commands

When things go wrong:

```bash
# Check recipe variables
bitbake -e hello-world | grep "^S="

# View compilation log
less tmp/work/aarch64-oe-linux/hello-world/1.0-*/temp/log.do_compile

# Enter development shell
bitbake -c devshell hello-world

# Clean and rebuild
bitbake -c clean hello-world
bitbake hello-world
```

---

## Common Issues

### File Not Found
```
ERROR: Fetcher failure for URL: 'file://hello-world.c'
```
**Fix:** Verify files exist in `recipes-apps/hello-world/files/`

### Compilation Fails
```
ERROR: do_compile: oe_runmake failed
```
**Fix:** Check log.do_compile for errors

### File Not Packaged
```
WARNING: File '/usr/bin/hello-world' not shipped
```
**Fix:** Add to `FILES:${PN}`

---

## Verification

```bash
# Recipe builds
bitbake hello-world && echo "SUCCESS"

# Binary is ARM
file tmp/work/aarch64-oe-linux/hello-world/1.0-*/image/usr/bin/hello-world | grep aarch64

# In image
grep hello-world tmp/deploy/images/*/core-image-minimal*.manifest
```

---

## Next Steps

- Modify the C code and rebuild
- Add runtime dependencies (RDEPENDS)
- Create Python recipe
- Proceed to Lab 03: Custom Meta-Layer

---

**Congratulations!** You've built your first custom recipe!

---

**Last Updated:** 2025-12-31
