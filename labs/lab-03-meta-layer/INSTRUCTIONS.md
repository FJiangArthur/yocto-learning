# Lab 03: Custom Meta-Layer - Instructions

## Step 1: Plan Layer Architecture
**Time:** 15 min | **Goal:** Define layer purpose and scope

Create a design document:
```bash
cat > ~/yocto-jetson/meta-lab-design.md << 'EOF'
# Meta-Lab Layer Design

## Purpose
Learning layer demonstrating proper Yocto layer structure

## Contents
- Custom machine configuration
- Example distro configuration  
- Sample recipes organized by category
- Documentation and examples

## Dependencies
- meta (Poky core)
- meta-openembedded (optional)
EOF
```

---

## Step 2: Create Layer Structure
**Time:** 20 min | **Goal:** Build complete directory tree

```bash
cd ~/yocto-jetson/poky
bitbake-layers create-layer ../meta-lab
cd ../meta-lab

# Create comprehensive structure
mkdir -p conf/{machine,distro}
mkdir -p recipes-{core,apps,kernel,bsp}/{images,packagegroups}
mkdir -p classes
mkdir -p docs

tree -L 2
```

**Expected:**
```
meta-lab/
├── conf/
│   ├── layer.conf
│   ├── machine/
│   └── distro/
├── recipes-core/
├── recipes-apps/
├── recipes-kernel/
├── recipes-bsp/
├── classes/
├── docs/
├── COPYING.MIT
└── README
```

---

## Step 3: Configure layer.conf
**Time:** 15 min | **Goal:** Set layer metadata

```bash
cat > conf/layer.conf << 'EOF'
# Layer configuration for meta-lab

BBPATH =. "${LAYERDIR}:"

BBFILES += "${LAYERDIR}/recipes-*/*/*.bb \
            ${LAYERDIR}/recipes-*/*/*.bbappend"

BBFILE_COLLECTIONS += "meta-lab"
BBFILE_PATTERN_meta-lab = "^${LAYERDIR}/"
BBFILE_PRIORITY_meta-lab = "10"

LAYERVERSION_meta-lab = "1"
LAYERDEPENDS_meta-lab = "core"
LAYERSERIES_COMPAT_meta-lab = "kirkstone"
EOF
```

**Key Variables:**
- `BBFILE_PRIORITY`: 10 = medium priority
- `LAYERDEPENDS`: Required layers
- `LAYERSERIES_COMPAT`: Compatible Yocto releases

---

## Step 4: Create Machine Configuration
**Time:** 20 min | **Goal:** Define custom machine

```bash
cat > conf/machine/lab-machine.conf << 'EOF'
#@TYPE: Machine
#@NAME: Lab Learning Machine
#@DESCRIPTION: Example machine configuration for learning

require conf/machine/qemuarm64.conf

MACHINE_FEATURES += "wifi bluetooth"

SERIAL_CONSOLES = "115200;ttyAMA0"

PREFERRED_PROVIDER_virtual/kernel = "linux-yocto"
PREFERRED_VERSION_linux-yocto = "5.10%"

IMAGE_FSTYPES = "tar.gz ext4"
EOF
```

---

## Step 5: Create Distro Configuration
**Time:** 20 min | **Goal:** Define distribution policies

```bash
cat > conf/distro/lab-distro.conf << 'EOF'
require conf/distro/poky.conf

DISTRO = "lab-distro"
DISTRO_NAME = "Lab Learning Distribution"
DISTRO_VERSION = "1.0"

MAINTAINER = "Your Name <your@email.com>"

INIT_MANAGER = "systemd"
DISTRO_FEATURES:append = " systemd"
DISTRO_FEATURES_BACKFILL_CONSIDERED = "sysvinit"

PACKAGE_CLASSES = "package_deb"
EOF
```

---

## Step 6: Add Sample Recipes
**Time:** 30 min | **Goal:** Organize recipes by category

```bash
# Create custom image recipe
mkdir -p recipes-core/images
cat > recipes-core/images/lab-image.bb << 'EOF'
SUMMARY = "Lab learning image"
LICENSE = "MIT"

inherit core-image

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

IMAGE_INSTALL += "\
    htop \
    vim \
    "

IMAGE_FEATURES += "ssh-server-openssh"
EOF

# Create packagegroup
mkdir -p recipes-core/packagegroups
cat > recipes-core/packagegroups/packagegroup-lab-tools.bb << 'EOF'
SUMMARY = "Lab development tools"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
    git \
    cmake \
    python3 \
    "
EOF
```

---

## Step 7: Create Documentation
**Time:** 20 min | **Goal:** Document layer usage

```bash
cat > README.md << 'EOF'
# Meta-Lab

Learning layer for Yocto Project development.

## Quick Start

1. Clone layer:
   ```
   git clone /path/to/meta-lab
   ```

2. Add to bblayers.conf:
   ```
   bitbake-layers add-layer ../meta-lab
   ```

3. Set machine and distro in local.conf:
   ```
   MACHINE = "lab-machine"
   DISTRO = "lab-distro"
   ```

4. Build:
   ```
   bitbake lab-image
   ```

## Contents

- Machine configs: conf/machine/
- Distro configs: conf/distro/
- Recipes: recipes-*/

## License

MIT - see COPYING.MIT
EOF

# Create license file
cp ../meta-custom/COPYING.MIT .
```

---

## Step 8: Add Layer to Build
**Time:** 10 min | **Goal:** Register layer

```bash
cd ~/yocto-jetson/builds/jetson-orin-agx
source ~/yocto-jetson/poky/oe-init-build-env .

bitbake-layers add-layer ~/yocto-jetson/meta-lab
bitbake-layers show-layers | grep meta-lab
```

---

## Step 9: Test Layer
**Time:** 20 min | **Goal:** Verify functionality

```bash
# Parse test
bitbake -p

# Set machine and distro
echo 'MACHINE = "lab-machine"' >> conf/local.conf
echo 'DISTRO = "lab-distro"' >> conf/local.conf

# Build custom image
bitbake lab-image
```

---

## Step 10: Version Control
**Time:** 15 min | **Goal:** Initialize Git repo

```bash
cd ~/yocto-jetson/meta-lab

git init
git add .
git commit -m "Initial meta-lab layer"

# Create .gitignore
cat > .gitignore << 'EOF'
*.pyc
*.swp
*~
EOF

git add .gitignore
git commit -m "Add gitignore"
```

---

## Verification

```bash
# Layer parses correctly
bitbake -p

# Layer appears in list
bitbake-layers show-layers | grep meta-lab

# Machine config valid
bitbake-getvar -r lab-machine MACHINE

# Image builds
bitbake lab-image
```

---

## Next Steps

- Add more recipes
- Create kernel bbappends
- Add custom classes
- Proceed to Lab 04: Jetson Image Customization

---

**Congratulations!** You've created a professional meta-layer!

---

**Last Updated:** 2025-12-31
