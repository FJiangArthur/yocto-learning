# Lab 01: First Yocto Build

## Overview
This lab guides you through your first complete Yocto Project build, from environment setup to generating a bootable image for NVIDIA Jetson platforms.

## Learning Objectives
By the end of this lab, you will:
- Understand the Yocto Project build environment structure
- Install and configure required dependencies
- Clone and configure the Poky reference distribution and meta-tegra layer
- Successfully build a minimal Linux image
- Verify the build output and understand generated artifacts

## Estimated Time
**2 hours** (plus additional build time - 1-4 hours depending on hardware)

## Prerequisites
- Linux development machine (Ubuntu 20.04/22.04 recommended) or macOS
- Minimum 100GB free disk space
- 8GB RAM minimum (16GB+ recommended)
- Stable internet connection
- Basic command-line knowledge

## What You'll Build
- **core-image-minimal**: A small console-only image that boots to a basic shell
- Target platform: NVIDIA Jetson (e.g., jetson-nano-devkit, jetson-xavier-nx-devkit)

## Lab Structure
1. **INSTRUCTIONS.md** - Complete step-by-step guide
2. **TROUBLESHOOTING.md** - Solutions to common issues
3. **VERIFICATION.md** - How to verify successful completion

## Key Concepts Covered
- Yocto Project layers and metadata
- BitBake build system
- BSP (Board Support Package) layers
- Build configuration files
- Image recipes and packages

## Next Steps
After completing this lab:
- Proceed to Lab 02 to create custom recipes
- Experiment with different image recipes (core-image-sato, core-image-full-cmdline)
- Try building for different MACHINE targets

## Resources
- [Yocto Project Quick Build Guide](https://docs.yoctoproject.org/brief-yoctoprojectqs/index.html)
- [meta-tegra Documentation](https://github.com/OE4T/meta-tegra)
- [BitBake User Manual](https://docs.yoctoproject.org/bitbake/)
