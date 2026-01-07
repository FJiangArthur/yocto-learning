# Lab 04: Jetson Image Customization - Instructions

## Step 1: Configure for Jetson Hardware
**Time:** 15 min | **Goal:** Set MACHINE to Jetson Orin

```bash
cd ~/yocto-jetson/builds/jetson-orin-agx
source ~/yocto-jetson/poky/oe-init-build-env .

# Edit local.conf
cat >> conf/local.conf << 'EOF'

# Jetson Orin Configuration
MACHINE = "jetson-orin-agx-devkit"

# Accept NVIDIA EULA
NVIDIA_EULA_ACCEPT = "1"

# Enable GPU and multimedia
CUDA_VERSION = "11.4"
TEGRA_MULTIMEDIA_SUPPORT = "1"
EOF
```

**Checkpoint:** ✅ MACHINE set to Jetson variant

---

## Step 2: Create Custom Image Recipe
**Time:** 30 min | **Goal:** Define Jetson-optimized image

```bash
mkdir -p ~/yocto-jetson/meta-custom/recipes-core/images

cat > ~/yocto-jetson/meta-custom/recipes-core/images/jetson-custom-image.bb << 'EOF'
SUMMARY = "Custom Jetson Orin Image"
DESCRIPTION = "Optimized image with GPU, AI/ML support"
LICENSE = "MIT"

inherit core-image

# Base system
IMAGE_INSTALL = "\
    packagegroup-core-boot \
    ${CORE_IMAGE_EXTRA_INSTALL} \
    "

# System utilities
IMAGE_INSTALL += "\
    systemd \
    openssh \
    htop \
    vim \
    python3 \
    "

# NVIDIA packages
IMAGE_INSTALL += "\
    cuda-libraries \
    cuda-toolkit \
    tensorrt \
    cudnn \
    "

# Development tools
IMAGE_INSTALL += "\
    git \
    cmake \
    gcc \
    g++ \
    python3-pip \
    python3-numpy \
    "

# Hardware tools
IMAGE_INSTALL += "\
    i2c-tools \
    can-utils \
    pciutils \
    usbutils \
    "

# Image features
IMAGE_FEATURES += "\
    ssh-server-openssh \
    debug-tweaks \
    package-management \
    "

# Extra space for development (2GB)
IMAGE_ROOTFS_EXTRA_SPACE = "2097152"
EOF
```

**Checkpoint:** ✅ Custom image recipe created

---

## Step 3: Configure Systemd Services
**Time:** 20 min | **Goal:** Set up auto-start services

```bash
mkdir -p ~/yocto-jetson/meta-custom/recipes-core/systemd-services/files

cat > ~/yocto-jetson/meta-custom/recipes-core/systemd-services/files/jetson-startup.service << 'EOF'
[Unit]
Description=Jetson Startup Service
After=network.target

[Service]
Type=oneshot
ExecStart=/usr/bin/jetson-startup.sh
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF

cat > ~/yocto-jetson/meta-custom/recipes-core/systemd-services/files/jetson-startup.sh << 'EOF'
#!/bin/bash
# Jetson startup script

echo "Jetson system starting..."

# Set power mode (maximum performance)
nvpmodel -m 0

# Set fan to auto mode
echo 50 > /sys/devices/pwm-fan/target_pwm

echo "Jetson startup complete"
EOF

chmod +x ~/yocto-jetson/meta-custom/recipes-core/systemd-services/files/jetson-startup.sh

# Create recipe
cat > ~/yocto-jetson/meta-custom/recipes-core/systemd-services/jetson-services_1.0.bb << 'EOF'
SUMMARY = "Jetson systemd services"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
    file://jetson-startup.service \
    file://jetson-startup.sh \
    "

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "jetson-startup.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/jetson-startup.service ${D}${systemd_unitdir}/system/

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/jetson-startup.sh ${D}${bindir}/
}

FILES:${PN} = "\
    ${systemd_unitdir}/system/jetson-startup.service \
    ${bindir}/jetson-startup.sh \
    "
EOF

# Add to image
cat >> ~/yocto-jetson/meta-custom/recipes-core/images/jetson-custom-image.bb << 'EOF'

IMAGE_INSTALL += "jetson-services"
EOF
```

**Checkpoint:** ✅ Systemd service created

---

## Step 4: Build the Image
**Time:** 2-3 hours | **Goal:** Compile Jetson image

```bash
cd ~/yocto-jetson/builds/jetson-orin-agx
source ~/yocto-jetson/poky/oe-init-build-env .

# Build custom image
bitbake jetson-custom-image

# Generate flashable image
bitbake -c do_image_tegraflash jetson-custom-image
```

**Checkpoint:** ✅ Build completes successfully

---

## Step 5: Verify Build Artifacts
**Time:** 10 min | **Goal:** Check generated files

```bash
cd tmp/deploy/images/jetson-orin-agx-devkit

ls -lh | grep jetson-custom-image

# Expected files:
# - jetson-custom-image-*.rootfs.tar.gz
# - jetson-custom-image-*.tegraflash.tar.gz
# - Image (kernel)
# - *.dtb (device trees)
```

**Checkpoint:** ✅ All artifacts present

---

## Step 6: Flash to Jetson Device
**Time:** 30 min | **Goal:** Deploy to hardware

```bash
# Extract flash package
mkdir ~/jetson-flash
cd ~/jetson-flash
tar xzf ~/yocto-jetson/builds/jetson-orin-agx/tmp/deploy/images/jetson-orin-agx-devkit/jetson-custom-image-*.tegraflash.tar.gz

# Put Jetson in recovery mode:
# 1. Power off
# 2. Hold RECOVERY button
# 3. Press POWER button
# 4. Release RECOVERY after 2 seconds

# Verify recovery mode
lsusb | grep -i nvidia
# Should show "NVIDIA Corp. APX"

# Flash
sudo ./doflash.sh

# Wait 10-15 minutes for flash to complete
```

**Checkpoint:** ✅ Flash completes successfully

---

## Step 7: First Boot Verification
**Time:** 15 min | **Goal:** Test on Jetson

```bash
# Connect via serial console or SSH
ssh root@jetson-orin-agx-devkit.local

# Once logged in:
uname -a
cat /etc/os-release

# Check GPU
nvidia-smi

# Check CUDA
nvcc --version

# Check TensorRT
dpkg -l | grep tensorrt

# Check systemd service
systemctl status jetson-startup.service
```

**Expected:** All services running, GPU detected

**Checkpoint:** ✅ System boots and GPU accessible

---

## Step 8: Performance Optimization
**Time:** 20 min | **Goal:** Tune for performance

```bash
# On Jetson, check current power mode
nvpmodel -q

# Set to maximum performance
sudo nvpmodel -m 0

# Check CPU frequencies
cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq

# Monitor temperatures
tegrastats
```

---

## Troubleshooting

### Issue: Flash Fails

**Solution:**
```bash
# Check USB connection
lsusb | grep NVIDIA

# Try manual flash
sudo ./flash.sh jetson-orin-agx-devkit mmcblk0p1
```

### Issue: GPU Not Detected

**Solution:**
```bash
# Verify CUDA packages installed
dpkg -l | grep cuda

# Check kernel modules
lsmod | grep nvidia

# Rebuild with CUDA enabled
echo 'CUDA_VERSION = "11.4"' >> conf/local.conf
bitbake jetson-custom-image
```

---

## Verification

See VERIFICATION.md for complete checklist.

Quick test:
```bash
# Image built
ls tmp/deploy/images/jetson-orin-agx-devkit/jetson-custom-image*

# On target: GPU works
nvidia-smi

# Services running
systemctl --failed
```

---

## Next Steps

- Optimize image size
- Add AI inference applications
- Proceed to Lab 05: Production Image

---

**Congratulations!** You've built a custom Jetson image!

---

**Last Updated:** 2025-12-31
