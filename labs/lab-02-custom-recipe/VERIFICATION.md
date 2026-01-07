# Lab 02: Custom Recipe Development - Verification

## Quick Verification

```bash
cd ~/yocto-jetson/builds/jetson-orin-agx
source ~/yocto-jetson/poky/oe-init-build-env .

# 1. Recipe builds successfully
bitbake hello-world
echo $?  # Should be 0

# 2. Binary is correct architecture
file tmp/work/aarch64-oe-linux/hello-world/1.0-*/image/usr/bin/hello-world
# Should show "ARM aarch64"

# 3. Package is in image manifest
grep hello-world tmp/deploy/images/*/core-image-minimal*.manifest
# Should show: hello-world aarch64 1.0
```

## Completion Checklist

- [ ] Recipe directory created in meta-custom layer
- [ ] hello-world.c source file written
- [ ] Makefile created with proper targets
- [ ] BitBake recipe (hello-world_1.0.bb) created
- [ ] meta-custom layer added to build
- [ ] Recipe parses without errors (bitbake -p)
- [ ] bitbake hello-world succeeds
- [ ] Binary is ARM aarch64 executable
- [ ] Package added to IMAGE_INSTALL
- [ ] core-image-minimal builds with package
- [ ] Application runs correctly in QEMU or on target

## Detailed Verification

### Recipe Structure
```bash
tree ~/yocto-jetson/meta-custom/recipes-apps/hello-world/
```
**Expected:**
```
hello-world/
├── files/
│   ├── hello-world.c
│   └── Makefile
└── hello-world_1.0.bb
```

### Build Artifacts
```bash
ls tmp/work/aarch64-oe-linux/hello-world/1.0-*/image/usr/bin/
```
**Expected:** hello-world binary present

### Package Contents
```bash
oe-pkgdata-util list-pkg-files hello-world
```
**Expected:** /usr/bin/hello-world

### Runtime Test
In QEMU or on target:
```bash
hello-world
```
**Expected:**
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

Built with Yocto on Dec 31 2025 12:34:56
```

## Success Criteria

✓ All checklist items completed
✓ Recipe builds without errors
✓ Application runs correctly
✓ Ready for Lab 03

---

**Last Updated:** 2025-12-31
