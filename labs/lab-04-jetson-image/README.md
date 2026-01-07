# Lab 04: Custom Jetson Image

## Overview
Build a customized Linux image specifically for NVIDIA Jetson platforms, including hardware-specific features, CUDA support, and optimized configurations.

## Learning Objectives
By the end of this lab, you will:
- Create Jetson-optimized custom images
- Configure MACHINE-specific settings
- Add CUDA and GPU acceleration packages
- Include Jetson-specific utilities and libraries
- Configure hardware interfaces (GPIO, I2C, SPI, etc.)
- Optimize image for specific use cases
- Handle Jetson firmware and bootloader customization

## Estimated Time
**2 hours** (plus build time)

## Prerequisites
- Completed Lab 01, 02, and 03
- Understanding of Yocto layers and recipes
- Familiarity with Jetson hardware
- meta-tegra layer configured

## What You'll Build
A production-ready custom image for Jetson with:
- CUDA runtime and development tools
- Computer vision libraries (OpenCV, VPI)
- Hardware interface support
- Optimized kernel configuration
- Custom applications and services

## Lab Structure
1. **README.md** (this file) - Overview
2. **INSTRUCTIONS.md** - Step-by-step guide
3. **jetson-custom-image.bb** - Custom image recipe
4. **TROUBLESHOOTING.md** - Common issues

## Key Concepts
- MACHINE-specific configurations
- CUDA integration in Yocto
- Device tree customization
- Kernel module selection
- Hardware acceleration
- Boot optimization

## Resources
- [meta-tegra Documentation](https://github.com/OE4T/meta-tegra)
- [NVIDIA Jetson Developer Guide](https://developer.nvidia.com/embedded/develop/software)
- [CUDA on Yocto](https://github.com/OE4T/meta-tegra/wiki)
