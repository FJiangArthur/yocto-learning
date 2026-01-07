# meta-interview-study

Custom Yocto layer for embedded Linux interview preparation and Jetson development.

## Overview

This layer provides a comprehensive collection of recipes, configurations, and tools for:

- **Jetson Platform Development**: Optimized for NVIDIA Jetson Orin AGX
- **AI/ML Workloads**: CUDA, TensorRT, and deep learning frameworks
- **Hardware Control**: GPIO, I2C, SPI, and custom device drivers
- **Interview Preparation**: Educational examples and best practices

## Layer Contents

### Recipes

**recipes-core/images/**
- `interview-study-image.bb` - Main development image

**recipes-example/hello/**
- `hello-world_1.0.bb` - Simple autotools example

**recipes-support/tools/**
- `yocto-utils_1.0.bb` - Development utilities

**recipes-gpu/** (when applicable)
- CUDA and TensorRT applications

### Configurations

**conf/machine/**
- `interview-jetson-orin.conf` - Custom Jetson Orin machine config

**conf/layer.conf**
- Layer metadata and configuration

### Classes

**classes/**
- `interview-common.bbclass` - Shared functionality across recipes

## Dependencies

Required layers:
- `meta` (OE-Core)
- `meta-openembedded/meta-oe`
- `meta-tegra` (for Jetson platforms)

## Quick Start

### 1. Add Layer to Build

```bash
# Clone the layer
cd /path/to/yocto/sources
git clone https://github.com/example/meta-interview-study.git

# Add to bblayers.conf
bitbake-layers add-layer meta-interview-study
```

### 2. Configure Build

In `conf/local.conf`:

```bash
# Set machine
MACHINE = "interview-jetson-orin"

# Optional: Enable features
INTERVIEW_STUDY_DEV_MODE = "1"
```

### 3. Build Image

```bash
bitbake interview-study-image
```

## Layer Configuration

### Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `INTERVIEW_STUDY_DEV_MODE` | `1` | Enable development features |
| `CUDA_ARCH` | `sm_87` | CUDA architecture for Orin |
| `TENSORRT_VERSION` | `8.6` | TensorRT version |
| `INTERVIEW_STUDY_PACKAGEGROUP` | `packagegroup-interview-study` | Default package group |

### Machine Features

The `interview-jetson-orin` machine provides:
- CUDA support (`cuda`)
- TensorRT optimization (`tensorrt`)
- GPIO control (`gpio-control`)
- NVMe storage (`nvme`)
- PCIe devices (`pcie`)
- Wireless connectivity (`wifi`, `bluetooth`)

## Development Workflow

### Build Individual Recipes

```bash
# Build specific recipe
bitbake hello-world

# Clean and rebuild
bitbake -c cleanall hello-world
bitbake hello-world

# Open development shell
bitbake hello-world -c devshell
```

### Layer Management

```bash
# Show layer info
bitbake-layers show-layers

# Show recipes from this layer
bitbake-layers show-recipes -i meta-interview-study

# Show layer dependencies
bitbake-layers show-depends
```

### Testing

```bash
# Generate SDK for cross-development
bitbake interview-study-image -c populate_sdk

# Run image tests
bitbake interview-study-image -c testimage
```

## Recipe Examples

### Simple Application Recipe

```bash
SUMMARY = "My custom application"
LICENSE = "MIT"

inherit interview-common autotools

SRC_URI = "file://myapp.c"

do_install:append() {
    interview_install_config "myapp.conf"
}
```

### CUDA Application Recipe

```bash
SUMMARY = "CUDA accelerated application"
LICENSE = "Apache-2.0"

inherit interview-common cuda

COMPATIBLE_MACHINE = "(tegra234)"

do_compile() {
    interview_cuda_compile
    ${CUDA_PATH}/bin/nvcc ${CUDA_NVCC_FLAGS} -o myapp myapp.cu
}
```

## Contributing

### Adding New Recipes

1. Place recipe in appropriate `recipes-*/` directory
2. Follow naming convention: `<name>_<version>.bb`
3. Inherit `interview-common` class for shared functionality
4. Add to appropriate packagegroup

### Adding Device Tree Overlays

1. Place `.dts` files in `recipes-kernel/linux/linux-tegra/`
2. Add to `KERNEL_DEVICETREE` in machine config
3. Document overlay purpose and pin assignments

### Testing Changes

```bash
# Validate layer
bitbake-layers show-overlayed

# Check recipe syntax
bitbake -e <recipe> | less

# Test build
bitbake <recipe>
```

## Troubleshooting

### Build Failures

```bash
# Check build errors
tail -f tmp/log/cooker/<machine>/console-latest.log

# Verbose build
bitbake -v <recipe>

# Show all tasks
bitbake -c listtasks <recipe>
```

### Layer Issues

```bash
# Verify layer compatibility
bitbake-layers show-layers | grep interview-study

# Check layer priority
bitbake-layers show-layers
```

### CUDA/GPU Issues

```bash
# Verify CUDA is enabled
bitbake -e | grep CUDA_ARCH

# Check machine features
bitbake -e | grep MACHINE_FEATURES
```

## Documentation

- [Yocto Project Documentation](https://docs.yoctoproject.org/)
- [meta-tegra Layer](https://github.com/OE4T/meta-tegra)
- [BitBake User Manual](https://docs.yoctoproject.org/bitbake/)

## License

This layer is released under the MIT License.

## Maintainers

- Interview Study Team <team@example.com>

## Version Compatibility

| Yocto Release | Status |
|---------------|--------|
| Kirkstone (4.0) | ✅ Tested |
| Langdale (4.1) | ✅ Tested |
| Mickledore (4.2) | ✅ Tested |
| Nanbield (4.3) | ✅ Tested |
| Scarthgap (5.0) | ✅ Tested |
