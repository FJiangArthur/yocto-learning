# Device Tree Examples for Jetson Platforms

## Table of Contents
1. [Introduction](#introduction)
2. [What are Device Trees?](#what-are-device-trees)
3. [Device Tree Syntax Basics](#device-tree-syntax-basics)
4. [Compiling Device Trees](#compiling-device-trees)
5. [Device Tree Overlays vs Full Device Trees](#device-tree-overlays-vs-full-device-trees)
6. [Jetson-Specific Device Tree Structure](#jetson-specific-device-tree-structure)
7. [Integrating Device Trees in Yocto](#integrating-device-trees-in-yocto)
8. [Directory Structure](#directory-structure)
9. [Quick Start Examples](#quick-start-examples)
10. [Troubleshooting](#troubleshooting)

---

## Introduction

This repository contains comprehensive device tree examples specifically designed for NVIDIA Jetson platforms, with a focus on Jetson Orin (Tegra234). These examples demonstrate how to configure hardware peripherals, enable interfaces, and integrate custom hardware through device tree modifications.

**Target Platforms:**
- NVIDIA Jetson Orin (Tegra234)
- NVIDIA Jetson Orin Nano
- NVIDIA Jetson Orin NX

**Prerequisites:**
- Basic understanding of Linux kernel concepts
- Familiarity with Jetson hardware architecture
- Yocto Project knowledge (for integration examples)

---

## What are Device Trees?

### Overview

A **Device Tree** is a data structure that describes the hardware components of a system in a way that the operating system can understand. Instead of hardcoding hardware information into the kernel, device trees provide a flexible, declarative way to describe:

- CPUs and memory layout
- Peripheral devices (UART, SPI, I2C, etc.)
- GPIO pin assignments
- Interrupt routing
- Clock configurations
- Power management settings
- Bus topology

### Why Device Trees Matter

1. **Hardware Abstraction**: Separates hardware description from kernel code
2. **Flexibility**: Same kernel binary works with different hardware configurations
3. **Maintainability**: Hardware changes don't require kernel recompilation
4. **Standardization**: Industry-standard format (Devicetree Specification)
5. **Bootloader Integration**: U-Boot and other bootloaders can modify device trees at runtime

### How Device Trees Work

```
┌─────────────────┐
│   Bootloader    │  Loads Device Tree Blob (DTB) into memory
│    (U-Boot)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Linux Kernel   │  Parses DTB and creates device drivers
│                 │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Device Drivers │  Probe and initialize hardware based on DTB
│                 │
└─────────────────┘
```

### Device Tree in the Boot Process

1. **Compile Time**: `.dts` source → Device Tree Compiler (dtc) → `.dtb` binary
2. **Boot Time**: Bootloader loads DTB into memory
3. **Kernel Init**: Kernel parses DTB and populates device tree data structures
4. **Driver Binding**: Kernel matches devices to drivers using `compatible` strings
5. **Runtime**: Drivers use device tree properties for configuration

---

## Device Tree Syntax Basics

### Basic Structure

A device tree is composed of **nodes** that form a hierarchical tree structure. Each node can contain **properties** that describe the hardware.

```dts
/dts-v1/;  /* Device tree version */

/ {  /* Root node */
    compatible = "nvidia,tegra234";
    #address-cells = <1>;
    #size-cells = <1>;

    cpus {
        #address-cells = <1>;
        #size-cells = <0>;

        cpu@0 {
            device_type = "cpu";
            compatible = "arm,cortex-a78";
            reg = <0>;
        };
    };

    memory@80000000 {
        device_type = "memory";
        reg = <0x80000000 0x40000000>;  /* 1GB at 0x80000000 */
    };
};
```

### Nodes

Nodes represent hardware devices or logical groupings. The format is:

```dts
node-name@unit-address {
    /* properties */

    /* child nodes */
};
```

**Examples:**
- `serial@3100000` - UART at address 0x3100000
- `i2c@3160000` - I2C controller at address 0x3160000
- `gpio@2200000` - GPIO controller at address 0x2200000

### Properties

Properties are name-value pairs that describe device characteristics:

```dts
property-name = <value>;
property-name = "string";
property-name = <0x12 0x34>;  /* Array of cells */
property-name;  /* Boolean (presence = true) */
```

#### Common Property Types

| Property Type | Example | Description |
|--------------|---------|-------------|
| Integer | `clock-frequency = <115200>;` | 32-bit integer |
| String | `compatible = "nvidia,tegra234-uart";` | Text string |
| Array | `reg = <0x3100000 0x10000>;` | Multiple integers |
| Boolean | `nvidia,enable-hw-flow-control;` | Flag property |
| Phandle | `clocks = <&bpmp_clks TEGRA234_CLK_UARTA>;` | Reference to another node |

### Standard Properties

#### compatible
Identifies the device and driver binding:
```dts
compatible = "vendor,device", "generic-device";
/* Example: */
compatible = "nvidia,tegra234-uart", "nvidia,tegra20-uart";
```

The kernel tries to match drivers in order from most specific to most generic.

#### reg
Specifies address and size of memory-mapped registers:
```dts
reg = <address size>;
/* Example: */
reg = <0x3100000 0x10000>;  /* UART at 0x3100000, 64KB size */
```

#### status
Indicates if device is enabled:
```dts
status = "okay";     /* Device is operational */
status = "disabled"; /* Device is present but disabled */
status = "fail";     /* Device failed */
```

#### interrupts
Specifies interrupt lines:
```dts
interrupts = <GIC_SPI 112 IRQ_TYPE_LEVEL_HIGH>;
```

#### clocks
References clock providers:
```dts
clocks = <&bpmp_clks TEGRA234_CLK_UARTA>;
clock-names = "serial";
```

### Phandles

Phandles are references to other nodes in the device tree:

```dts
/* Define a node with a label */
uart0: serial@3100000 {
    compatible = "nvidia,tegra234-uart";
    reg = <0x3100000 0x10000>;
};

/* Reference it using phandle */
bluetooth {
    compatible = "brcm,bcm4354";
    uart = <&uart0>;  /* Reference using label */
};
```

### Labels and Paths

Labels provide convenient references:
```dts
/* Label definition */
uart0: serial@3100000 { ... };

/* Reference by label */
<&uart0>

/* Reference by path */
&{/serial@3100000}
```

### Address Cells and Size Cells

These properties define how to interpret `reg` properties:

```dts
soc {
    #address-cells = <1>;  /* Address is 1 cell (32-bit) */
    #size-cells = <1>;     /* Size is 1 cell (32-bit) */

    uart@3100000 {
        reg = <0x3100000 0x10000>;
        /* ^address    ^size    */
    };
};
```

For 64-bit addressing:
```dts
#address-cells = <2>;  /* Address needs 2 cells (64-bit) */
#size-cells = <2>;     /* Size needs 2 cells (64-bit) */

memory@80000000 {
    reg = <0x0 0x80000000  0x0 0x40000000>;
    /*     ^high ^low      ^high ^low      */
};
```

---

## Available Overlays

### Legacy Examples (From Previous Version)

### 1. jetson-orin-gpio-overlay.dts
**Purpose**: Configure GPIO pins for LED, button, and interrupt handling

**Features**:
- LED control (gpio-leds driver)
- Button input (gpio-keys driver)
- Interrupt-capable GPIO for sensors
- Pinmux configuration

**Use cases**:
- User interface (LEDs, buttons)
- Motion/proximity sensors
- Custom GPIO applications

### 2. jetson-orin-i2c-sensor.dts
**Purpose**: Configure I2C sensors on the 40-pin header

**Supported devices**:
- BME280: Temperature/humidity/pressure sensor
- MPU6050: 6-axis IMU (accelerometer + gyroscope)
- INA219: Current/power monitor
- EEPROM (24C256): Non-volatile storage
- PCA9685: 16-channel PWM controller

**Use cases**:
- Environmental monitoring
- Motion tracking and robotics
- Power monitoring
- Data logging

### 3. jetson-orin-spi-display.dts
**Purpose**: Configure SPI displays and flash memory

**Supported devices**:
- ST7789V: 240x320 SPI LCD display
- ILI9341: 320x240 SPI LCD display
- W25Q128: 16MB SPI NOR flash
- MCP3008: 8-channel ADC

**Use cases**:
- User interface displays
- Data storage and logging
- Analog sensor reading
- Visual feedback systems

## Compilation

### Prerequisites
```bash
sudo apt-get install device-tree-compiler
```

### Compile an overlay
```bash
# Compile DTS to DTBO
dtc -@ -I dts -O dtb -o jetson-orin-gpio-overlay.dtbo jetson-orin-gpio-overlay.dts

# Check for errors
dtc -@ -I dts -O dtb -o /dev/null jetson-orin-gpio-overlay.dts

# Decompile to verify (optional)
dtc -I dtb -O dts -o test.dts jetson-orin-gpio-overlay.dtbo
```

### Compiler options
- `-@`: Enable overlay syntax (required for overlays)
- `-I dts`: Input format is DTS source
- `-O dtb`: Output format is DTB binary
- `-o <file>`: Output file name

## Installation

### Method 1: Using jetson-io (Recommended)
```bash
# Install overlay to system directory
sudo cp jetson-orin-gpio-overlay.dtbo /boot/dtb/

# Run jetson-io configuration tool
sudo /opt/nvidia/jetson-io/jetson-io.py

# Select "Configure 40-pin expansion header"
# Choose your custom overlay from the list
# Save and reboot
```

### Method 2: Manual extlinux.conf
```bash
# Copy overlay to boot partition
sudo cp *.dtbo /boot/dtb/

# Edit extlinux configuration
sudo nano /boot/extlinux/extlinux.conf

# Add FDTOVERLAYS line:
LABEL primary
      MENU LABEL primary kernel
      LINUX /boot/Image
      FDT /boot/dtb/kernel_tegra234-p3701-0000-p3737-0000.dtb
      FDTOVERLAYS /boot/dtb/jetson-orin-gpio-overlay.dtbo /boot/dtb/jetson-orin-i2c-sensor.dtbo
      INITRD /boot/initrd
      APPEND ${cbootargs} root=/dev/mmcblk0p1 rw rootwait rootfstype=ext4

# Save and reboot
sudo reboot
```

### Method 3: Yocto Integration
Add to your layer's `recipes-kernel/linux/linux-tegra_%.bbappend`:

```bitbake
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://jetson-orin-gpio-overlay.dts \
    file://jetson-orin-i2c-sensor.dts \
    file://jetson-orin-spi-display.dts \
"

do_compile:append() {
    # Compile device tree overlays
    for dts in ${WORKDIR}/*.dts; do
        dtc -@ -I dts -O dtb -o ${B}/arch/arm64/boot/dts/nvidia/$(basename $dts .dts).dtbo $dts
    done
}

do_install:append() {
    install -d ${D}/boot/dtb
    install -m 0644 ${B}/arch/arm64/boot/dts/nvidia/*.dtbo ${D}/boot/dtb/
}

FILES:${KERNEL_PACKAGE_NAME}-devicetree += "/boot/dtb/*.dtbo"
```

## Verification

### Check overlay is loaded
```bash
# Verify overlay file exists
ls -l /boot/dtb/*.dtbo

# Check device tree nodes
ls /proc/device-tree/
cat /proc/device-tree/compatible

# For GPIO overlay
ls /sys/class/leds/
ls /sys/class/gpio/

# For I2C overlay
i2cdetect -y -r 2
ls /sys/bus/i2c/devices/

# For SPI overlay
ls /dev/spidev*
cat /sys/class/spi_master/spi*/device/modalias
```

### Test functionality

#### GPIO Testing
```bash
# LED control
echo 1 > /sys/class/leds/user-led:green/brightness
echo 0 > /sys/class/leds/user-led:green/brightness

# Button events
evtest /dev/input/event0  # Press button to see events
```

#### I2C Testing
```bash
# Scan I2C bus
i2cdetect -y 2

# Read sensor (BME280 example)
cat /sys/bus/iio/devices/iio:device0/in_temp_input
cat /sys/bus/iio/devices/iio:device0/in_pressure_input
```

#### SPI Testing
```bash
# Display framebuffer info
fbset -i

# Clear display
dd if=/dev/zero of=/dev/fb0 bs=4096

# SPI flash info (if loaded)
cat /proc/mtd
flashrom -p linux_spi:dev=/dev/spidev1.1
```

## Debugging

### Common issues

#### 1. Overlay not loading
```bash
# Check kernel log for errors
dmesg | grep -i "overlay\|device tree"

# Verify DTC compilation was successful
dtc -@ -I dts -O dtb -o /dev/null youroverlay.dts

# Check overlay syntax
dtc -I dtb -O dts youroverlay.dtbo | less
```

#### 2. Device not appearing
```bash
# Check if driver is loaded
lsmod | grep <driver_name>

# Load driver manually
modprobe <driver_name>

# Check driver binding
ls -l /sys/bus/*/devices/*/driver

# Kernel log for probe errors
dmesg | grep -i "probe\|failed"
```

#### 3. Wrong GPIO/I2C/SPI bus
```bash
# List all GPIO chips
gpiodetect

# List all I2C buses
i2cdetect -l

# List all SPI buses
ls /sys/class/spi_master/
```

## Pin Mapping Reference

### 40-Pin Header Pinout
```
    3.3V  (1) (2)  5V
   I2C_SDA (3) (4)  5V
   I2C_SCL (5) (6)  GND
   GPIO09  (7) (8)  UART_TX
     GND   (9) (10) UART_RX
  UART_RTS (11) (12) I2S_SCLK
  SPI1_SCK (13) (14) GND
   GPIO12  (15) (16) SPI1_CS1
    3.3V   (17) (18) SPI1_CS0
 SPI0_MOSI (19) (20) GND
 SPI0_MISO (21) (22) GPIO27
  SPI0_SCK (23) (24) SPI0_CS0
     GND   (25) (26) SPI0_CS1
  I2C1_SDA (27) (28) I2C1_SCL
   GPIO05  (29) (30) GND
   GPIO06  (31) (32) GPIO07
   GPIO13  (33) (34) GND
  I2S0_FS  (35) (36) UART_CTS
   GPIO26  (37) (38) I2S0_DIN
     GND   (39) (40) I2S0_DOUT
```

### Jetson Orin GPIO Banks
- Main GPIO: tegra_main_gpio (316-579)
- AON GPIO: tegra_aon_gpio (0-41)

## Best Practices

1. **Always test overlays before deployment**
   - Compile and check for warnings
   - Test on development board first
   - Verify no conflicts with existing hardware

2. **Use descriptive labels**
   - Name GPIOs clearly (e.g., "user-led", "motion-sensor")
   - Document pin assignments in comments
   - Create clear compatible strings

3. **Handle conflicts gracefully**
   - Check pinmux doesn't conflict with other functions
   - Verify I2C/SPI addresses don't clash
   - Test interrupt assignments

4. **Version control your overlays**
   - Track changes in git
   - Document hardware revisions
   - Maintain compatibility matrix

5. **Power considerations**
   - Verify voltage levels (3.3V vs 5V)
   - Check current requirements
   - Add proper pull-ups/pull-downs

## Resources

- [Linux Device Tree Documentation](https://www.kernel.org/doc/Documentation/devicetree/)
- [NVIDIA Jetson Linux Developer Guide](https://docs.nvidia.com/jetson/archives/)
- [Device Tree Overlay Documentation](https://www.kernel.org/doc/Documentation/devicetree/overlay-notes.txt)
- [Tegra234 Technical Reference Manual](https://developer.nvidia.com/embedded/downloads)

## Contributing

When adding new overlays:
1. Follow existing naming conventions
2. Add detailed comments explaining functionality
3. Include testing procedures
4. Update this README with new overlay description
5. Provide wiring diagrams if needed

## License

These device tree overlays are provided under the MIT License.
