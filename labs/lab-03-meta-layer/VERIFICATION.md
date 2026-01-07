# Lab 03: Custom Meta Layer Verification Guide

## Verification Checklist

Use this guide to verify your custom meta-layer is properly configured and functional.

---

## 1. Layer Structure Verification

**Goal:** Confirm directory structure is correct

```bash
cd ~/yocto-builds/jetson-first-build/meta-lab

# Check directory structure
tree -L 3 .
```

**Expected Structure:**
```
meta-lab/
├── conf/
│   └── layer.conf
├── recipes-core/
│   └── images/
│       └── lab-custom-image.bb
├── recipes-apps/
│   └── myapp/
│       ├── myapp_1.0.bb
│       └── files/
├── recipes-extended/
│   └── sudo/
│       ├── sudo_%.bbappend
│       └── files/
└── README.md
```

**Checkpoint:** ✅ All directories and files present

---

## 2. layer.conf Validation

**Goal:** Verify layer configuration is correct

```bash
# Check layer.conf syntax
cat conf/layer.conf

# Verify required variables are set
grep "BBFILE_COLLECTIONS" conf/layer.conf
grep "BBFILE_PRIORITY" conf/layer.conf
grep "LAYERSERIES_COMPAT" conf/layer.conf
```

**Required Variables:**
- `BBPATH` - Includes layer directory
- `BBFILES` - Includes recipes pattern
- `BBFILE_COLLECTIONS += "meta-lab"`
- `BBFILE_PRIORITY_meta-lab = "8"`
- `LAYERSERIES_COMPAT_meta-lab = "kirkstone"`

**Checkpoint:** ✅ All required variables present and correct

---

## 3. Layer Integration Verification

**Goal:** Confirm layer is added to build

```bash
cd ~/yocto-builds/jetson-first-build/build-jetson
source ../poky/oe-init-build-env .

# Show all layers
bitbake-layers show-layers

# Should show meta-lab with priority 8
```

**Expected Output:**
```
layer             path                              priority
================================================================
...
meta-lab          .../meta-lab                      8
```

**Checkpoint:** ✅ meta-lab appears in layer list

---

## 4. Recipe Visibility Verification

**Goal:** Verify recipes from layer are visible to BitBake

```bash
# Show recipes provided by meta-lab
bitbake-layers show-recipes -i meta-lab

# Check for specific recipes
bitbake-layers show-recipes myapp
bitbake-layers show-recipes lab-custom-image
```

**Expected Output:**
```
myapp:
  meta-lab                 1.0

lab-custom-image:
  meta-lab                 1.0
```

**Checkpoint:** ✅ All layer recipes are visible

---

## 5. Custom Application Build Verification

**Goal:** Build and verify myapp recipe

```bash
# Clean and build
bitbake myapp -c cleanall
bitbake myapp

# Check build success
echo $?
# Should be 0

# Find binary
find tmp/work -name "myapp" -type f -executable

# Verify binary type
file tmp/work/*/myapp/*/build/myapp
# Expected: ELF 64-bit LSB executable, ARM aarch64
```

**Checkpoint:** ✅ myapp builds successfully

---

## 6. bbappend File Verification

**Goal:** Verify sudo bbappend is applied

```bash
# Show sudo recipe with appends
bitbake-layers show-appends | grep sudo

# Should show:
# sudo_X.X.X.bb:
#   .../meta-lab/recipes-extended/sudo/sudo_%.bbappend

# Build sudo to test bbappend
bitbake sudo -c cleanall
bitbake sudo

# Check if custom sudoers file was added
find tmp/work -path "*/sudo/*/image/etc/sudoers.d/10-custom"
```

**Checkpoint:** ✅ bbappend is applied and custom file is installed

---

## 7. Custom Image Build Verification

**Goal:** Build complete custom image

```bash
# Build custom image
bitbake lab-custom-image

# Check build success
echo $?
# Should be 0

# Verify image files exist
ls tmp/deploy/images/*/lab-custom-image*
```

**Expected Files:**
- `lab-custom-image-*.tar.gz`
- `lab-custom-image-*.ext4`
- `lab-custom-image-*.manifest`
- `lab-custom-image-*.tegraflash.tar.gz`

**Checkpoint:** ✅ Custom image builds successfully

---

## 8. Image Content Verification

**Goal:** Verify custom packages are in image

```bash
# Check image manifest
cat tmp/deploy/images/*/lab-custom-image-*.manifest | grep myapp
# Should show: myapp

# Check for other packages
cat tmp/deploy/images/*/lab-custom-image-*.manifest | grep -E 'sudo|vim|htop|openssh'

# Count total packages
wc -l tmp/deploy/images/*/lab-custom-image-*.manifest
```

**Checkpoint:** ✅ Custom packages appear in manifest

---

## 9. Rootfs Extraction Verification

**Goal:** Examine actual rootfs content

```bash
cd tmp/deploy/images/jetson-nano-devkit/

# Extract rootfs
mkdir -p /tmp/lab03-verify
sudo tar -xzf lab-custom-image-*.tar.gz -C /tmp/lab03-verify

# Verify myapp binary
ls -la /tmp/lab03-verify/usr/bin/myapp
file /tmp/lab03-verify/usr/bin/myapp

# Verify sudo configuration
ls -la /tmp/lab03-verify/etc/sudoers.d/10-custom
cat /tmp/lab03-verify/etc/sudoers.d/10-custom

# Verify other tools
ls /tmp/lab03-verify/usr/bin/vim
ls /tmp/lab03-verify/usr/bin/htop

# Cleanup
sudo rm -rf /tmp/lab03-verify
```

**Checkpoint:** ✅ All expected files present in rootfs

---

## 10. Layer Dependency Verification

**Goal:** Check layer dependencies are satisfied

```bash
# Show layer dependencies
bitbake-layers layerindex-show-depends meta-lab

# Test removing core dependency (should fail)
# This is just a test - we'll re-add it
bitbake-layers remove-layer meta-lab
bitbake-layers add-layer ../meta-lab
```

**Checkpoint:** ✅ Dependencies are correctly configured

---

## 11. Layer Priority Verification

**Goal:** Verify layer priority is correct

```bash
# Check priority
bitbake-layers show-layers | grep meta-lab
# Should show priority 8

# Verify priority affects recipe selection
# If two layers provide same recipe, higher priority wins
bitbake -e myapp | grep "^FILE="
# Should point to meta-lab version
```

**Checkpoint:** ✅ Layer priority is 8 and working correctly

---

## 12. Layer Compatibility Verification

**Goal:** Verify layer works with current Yocto release

```bash
# Check build environment release
bitbake -e | grep "^DISTRO_CODENAME="
# Should be "kirkstone"

# Verify layer compatibility matches
grep LAYERSERIES_COMPAT ../meta-lab/conf/layer.conf
# Should include "kirkstone"
```

**Checkpoint:** ✅ Layer is compatible with current release

---

## 13. Hardware Testing (Jetson Device)

**Goal:** Test on actual hardware

**Flash custom image:**
```bash
cd tmp/deploy/images/jetson-nano-devkit/
tar -xzf lab-custom-image-*.tegraflash.tar.gz
sudo ./flash.sh jetson-nano-devkit mmcblk0p1
```

**On Jetson:**
```bash
# Test myapp
myapp
# Expected: "My App from meta-lab layer!"

# Test sudo configuration
groups
# Add user to wheel group if needed
# sudo usermod -aG wheel username

# Test sudo without password (if in wheel group)
sudo whoami
# Should not prompt for password if configuration is applied
```

**Checkpoint:** ✅ Custom applications and configurations work on hardware

---

## 14. Layer Removal and Re-addition

**Goal:** Test layer can be cleanly managed

```bash
cd ~/yocto-builds/jetson-first-build/build-jetson

# Remove layer
bitbake-layers remove-layer meta-lab
bitbake-layers show-layers | grep meta-lab
# Should show nothing

# Verify recipes are gone
bitbake-layers show-recipes myapp
# Should show: NOTE: No recipes available for: myapp

# Re-add layer
bitbake-layers add-layer ../../meta-lab
bitbake-layers show-layers | grep meta-lab
# Should show meta-lab again

# Verify recipes are back
bitbake-layers show-recipes myapp
# Should show recipe again
```

**Checkpoint:** ✅ Layer can be removed and re-added cleanly

---

## 15. Build Reproducibility Verification

**Goal:** Ensure layer builds consistently

```bash
# Clean all
bitbake lab-custom-image -c cleanall

# Build again
bitbake lab-custom-image

# Compare with previous build
# Checksums should be consistent
```

**Checkpoint:** ✅ Builds are reproducible

---

## Final Verification Summary

### Complete Success Criteria

Mark each as complete:

- [ ] Layer directory structure is correct
- [ ] layer.conf has all required variables
- [ ] Layer appears in bitbake-layers show-layers
- [ ] All recipes are visible to BitBake
- [ ] myapp builds successfully
- [ ] bbappend is applied to sudo
- [ ] Custom image builds successfully
- [ ] Custom packages appear in image manifest
- [ ] Files exist in extracted rootfs
- [ ] Layer dependencies are satisfied
- [ ] Layer priority is correct (8)
- [ ] Layer compatibility matches release
- [ ] Applications work on target hardware
- [ ] Layer can be removed/re-added cleanly
- [ ] Builds are reproducible

---

## Troubleshooting Failed Verification

### Layer not found
- Check path when adding: `bitbake-layers add-layer <correct-path>`
- Verify layer.conf exists
- Check BBFILE_COLLECTIONS matches layer name

### Recipes not visible
- Verify BBFILES pattern in layer.conf
- Check recipe naming (*.bb extension)
- Ensure recipes are in recipes-*/ directories

### Build fails
- Check recipe syntax
- Verify source files exist in files/ subdirectories
- Review build logs in tmp/work/.../temp/

### bbappend not applied
- Check recipe name matches (sudo_%.bbappend)
- Verify FILESEXTRAPATHS in bbappend
- Ensure layer priority is high enough

---

## Save Your Work

```bash
# Backup layer
tar -czf ~/meta-lab-lab03-$(date +%Y%m%d).tar.gz \
    ~/yocto-builds/jetson-first-build/meta-lab/

# Save configuration
cp build-jetson/conf/bblayers.conf \
    ~/meta-lab-bblayers-lab03.conf
```

**Checkpoint:** ✅ Layer backed up successfully

---

## Congratulations!

If all verification steps pass, you have successfully:
- Created a properly structured custom meta-layer
- Configured layer metadata correctly
- Organized recipes by category
- Extended existing recipes with bbappends
- Built custom images with your layer
- Integrated your layer into the build system

**You are ready for Lab 04: Jetson-Specific Custom Images!**
