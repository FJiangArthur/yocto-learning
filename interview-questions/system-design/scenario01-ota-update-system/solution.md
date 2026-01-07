# Solution: OTA Update System Design

## Executive Summary

This solution proposes a dual-strategy OTA update system using **SWUpdate** for atomic system updates and **Docker** for application container updates, integrated with a custom fleet management backend.

**Key Design Decisions:**
- A/B partition scheme for atomic updates and fast rollback
- SWUpdate for system-level updates (Yocto-generated)
- Delta updates to minimize bandwidth
- Staged rollout with canary testing
- Automatic rollback on boot failure

---

## 1. Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Update Server                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Image      │  │  Signature   │  │   Fleet      │          │
│  │   Builder    │  │  Service     │  │  Manager     │          │
│  │  (Jenkins)   │  │  (HSM)       │  │  (Backend)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                  │
│         v                  v                  v                  │
│  ┌─────────────────────────────────────────────────────┐        │
│  │            CDN / Update Repository                   │        │
│  └─────────────────────────────────────────────────────┘        │
└────────────────────────────────┬────────────────────────────────┘
                                 │ HTTPS
                                 v
┌─────────────────────────────────────────────────────────────────┐
│                    Edge Device (Jetson)                         │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                    Boot Partition                       │    │
│  │  U-Boot + env (bootcount, active partition)            │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌────────────────────┐        ┌────────────────────┐          │
│  │  Partition A       │        │  Partition B       │          │
│  │  (Active)          │        │  (Inactive)        │          │
│  │ ┌────────────────┐ │        │ ┌────────────────┐ │          │
│  │ │ Kernel         │ │        │ │ Kernel         │ │          │
│  │ │ Root FS        │ │        │ │ Root FS        │ │          │
│  │ │ SWUpdate       │ │        │ │ SWUpdate       │ │          │
│  │ └────────────────┘ │        │ └────────────────┘ │          │
│  └────────────────────┘        └────────────────────┘          │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │            Data Partition (Persistent)                  │    │
│  │  - Application configs                                  │    │
│  │  - AI models                                            │    │
│  │  - Logs                                                 │    │
│  │  - Update status                                        │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                 Update Agent                            │    │
│  │  - Check for updates                                    │    │
│  │  - Download updates                                     │    │
│  │  - Verify signatures                                    │    │
│  │  - Install to inactive partition                        │    │
│  │  - Trigger reboot                                       │    │
│  │  - Report status                                        │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

### OTA Framework: SWUpdate

**Choice: SWUpdate**

**Rationale:**
1. **Lightweight**: Minimal resource footprint (critical for Jetson Nano)
2. **Flexible**: Supports full images, delta updates, scripts
3. **Secure**: Built-in signature verification, encryption support
4. **Yocto Integration**: meta-swupdate layer well-maintained
5. **Bootloader Support**: Excellent U-Boot integration
6. **Active Community**: Strong community support

**Alternatives Considered:**

| Framework | Pros | Cons | Decision |
|-----------|------|------|----------|
| **Mender** | Great UI, hosted service | Heavy (Go daemon), subscription cost | Rejected - resource heavy |
| **RAUC** | Simple, robust | Less flexible update formats | Considered, but SWUpdate more versatile |
| **OSTree** | Git-like updates | Complex setup, different paradigm | Rejected - complexity |
| **Custom** | Full control | High dev cost, maintenance | Rejected - reinventing wheel |

### Additional Components

```yaml
Backend:
  - Fleet Management: Custom (Python/Django)
  - Update Storage: AWS S3 + CloudFront CDN
  - Database: PostgreSQL (device registry)
  - Message Queue: RabbitMQ (update notifications)
  - Monitoring: Prometheus + Grafana

Device Agent:
  - SWUpdate: Core update engine
  - Update Client: Custom (C++, systemd service)
  - Health Monitor: Custom (monitors system health)
  - Metrics Reporter: Telegraf → InfluxDB

Security:
  - Code Signing: HSM-backed private key
  - TLS: Let's Encrypt certificates
  - VPN: WireGuard (for support access)
```

---

## 3. Storage Layout

### Partition Scheme

```
/dev/mmcblk0 (32GB eMMC)
├── /dev/mmcblk0p1  (64MB)   - Boot (U-Boot, env)
├── /dev/mmcblk0p2  (12GB)   - Root A (active)
├── /dev/mmcblk0p3  (12GB)   - Root B (inactive)
└── /dev/mmcblk0p4  (8GB)    - Data (persistent)

/dev/mmcblk1 (128GB SD Card)
├── /dev/mmcblk1p1  (100GB)  - Docker volumes
└── /dev/mmcblk1p2  (28GB)   - Update cache, logs
```

**Partition Sizing Rationale:**

- **Root A/B (12GB each)**: Allows full system (2GB) + ~10GB headroom
- **Data (8GB)**: Configs, models, persistent data
- **Boot (64MB)**: U-Boot (~1MB), environment, devicetree
- **SD Card**: Docker images, update staging, logs

### WIC Kickstart Configuration

```bash
# meta-custom/scripts/jetson-ota.wks

# Boot partition (U-Boot)
part /boot --source bootimg-partition --fstype=vfat --label boot --active --align 4096 --size 64M --extra-space 0

# Root filesystem A
part / --source rootfs --fstype=ext4 --label rootA --align 4096 --size 12G --extra-space 0

# Root filesystem B (empty initially)
part --fstype=ext4 --label rootB --align 4096 --size 12G

# Persistent data partition
part /data --fstype=ext4 --label data --align 4096 --size 8G

bootloader --ptable gpt --append="rootwait console=ttyS0,115200"
```

---

## 4. Update Process Flow

### Detailed Update Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Update Check Phase                                       │
│    ┌─────────────────────────────────────────────────┐     │
│    │ Update Agent (every 4 hours)                    │     │
│    │  - Check fleet manager for updates              │     │
│    │  - Send device status (version, health)         │     │
│    │  - Receive update manifest if available         │     │
│    └─────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                          v
┌─────────────────────────────────────────────────────────────┐
│ 2. Download Phase                                           │
│    ┌─────────────────────────────────────────────────┐     │
│    │ - Download .swu file from CDN                   │     │
│    │ - Resume support (HTTP range requests)          │     │
│    │ - Verify checksum during download               │     │
│    │ - Store in /data/updates/                       │     │
│    └─────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                          v
┌─────────────────────────────────────────────────────────────┐
│ 3. Verification Phase                                       │
│    ┌─────────────────────────────────────────────────┐     │
│    │ - Verify digital signature (RSA 4096)           │     │
│    │ - Check update compatibility                    │     │
│    │ - Verify available space                        │     │
│    │ - Pre-flight checks passed                      │     │
│    └─────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                          v
┌─────────────────────────────────────────────────────────────┐
│ 4. Installation Phase                                       │
│    ┌─────────────────────────────────────────────────┐     │
│    │ - Determine inactive partition (e.g., B)        │     │
│    │ - SWUpdate writes to /dev/mmcblk0p3             │     │
│    │ - Update U-Boot environment:                    │     │
│    │   * Set boot_target=B                           │     │
│    │   * Set bootcount=0                             │     │
│    │ - Write update status to /data/                 │     │
│    └─────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                          v
┌─────────────────────────────────────────────────────────────┐
│ 5. Reboot Phase                                             │
│    ┌─────────────────────────────────────────────────┐     │
│    │ - Gracefully stop applications                  │     │
│    │ - Sync filesystems                              │     │
│    │ - Reboot                                        │     │
│    └─────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                          v
┌─────────────────────────────────────────────────────────────┐
│ 6. Boot & Validation Phase                                  │
│    ┌─────────────────────────────────────────────────┐     │
│    │ U-Boot:                                         │     │
│    │  - Increment bootcount                          │     │
│    │  - Boot from partition B                        │     │
│    │  - If bootcount > 3: revert to A                │     │
│    │                                                  │     │
│    │ Linux Init:                                     │     │
│    │  - Start health monitor                         │     │
│    │  - Validate critical services                   │     │
│    │  - Check AI model loads                         │     │
│    │  - Run smoke tests                              │     │
│    └─────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                ┌─────────┴─────────┐
                │                   │
                v                   v
        ┌────────────┐      ┌────────────┐
        │  Success   │      │  Failure   │
        └────────────┘      └────────────┘
                │                   │
                v                   v
┌──────────────────────┐  ┌──────────────────────┐
│ 7a. Commit Phase     │  │ 7b. Rollback Phase   │
│  - Reset bootcount=0 │  │  - Auto-rollback     │
│  - Mark partition OK │  │  - Boot from A       │
│  - Report success    │  │  - Report failure    │
│  - Cleanup old image │  │  - Collect logs      │
└──────────────────────┘  └──────────────────────┘
```

---

## 5. Rollback Strategy

### Automatic Rollback Triggers

1. **Boot Failure** (U-Boot level)
   - If bootcount > 3: revert to previous partition
   - U-Boot env: `bootlimit=3`

2. **System Health Failure** (Init level)
   - Critical services failed to start
   - Health check script exits with error
   - Watchdog timeout

3. **Application Failure** (Runtime level)
   - AI inference fails
   - Key containers won't start
   - Monitored metrics out of bounds

### Rollback Implementation

**U-Boot Configuration:**
```bash
# U-Boot environment
bootlimit=3
upgrade_available=1
boot_partition=B

# Boot script logic
if test ${bootcount} -gt ${bootlimit}; then
    echo "Boot failed ${bootcount} times, rolling back"
    setenv boot_partition A
    setenv upgrade_available 0
    saveenv
fi

# Boot from designated partition
ext4load mmc 0:${boot_partition} ${kernel_addr} /boot/Image
booti ${kernel_addr} - ${fdt_addr}
```

**Health Check Service:**
```python
# /usr/bin/health-check.py

import sys
import subprocess

def check_critical_services():
    services = ['docker', 'ai-inference', 'network-manager']
    for service in services:
        result = subprocess.run(['systemctl', 'is-active', service],
                              capture_output=True)
        if result.returncode != 0:
            return False
    return True

def check_ai_model():
    # Test model inference
    try:
        result = subprocess.run(['/usr/bin/model-test'],
                              timeout=30)
        return result.returncode == 0
    except:
        return False

if __name__ == '__main__':
    if not check_critical_services():
        sys.exit(1)
    if not check_ai_model():
        sys.exit(1)

    # All checks passed - commit update
    subprocess.run(['/usr/bin/fw_setenv', 'bootcount', '0'])
    subprocess.run(['/usr/bin/fw_setenv', 'upgrade_available', '0'])
    sys.exit(0)
```

---

## 6. Yocto Integration

### Layer Structure

```
meta-custom-ota/
├── conf/
│   └── layer.conf
├── classes/
│   └── custom-swupdate.bbclass
├── recipes-bsp/
│   └── u-boot/
│       ├── u-boot-%.bbappend
│       └── files/
│           └── bootcount.cfg
├── recipes-support/
│   ├── swupdate/
│   │   ├── swupdate_%.bbappend
│   │   └── files/
│   │       ├── swupdate.cfg
│   │       └── sw-update-check.service
│   └── update-agent/
│       ├── update-agent_1.0.bb
│       └── files/
│           ├── update-agent.cpp
│           └── update-agent.service
├── recipes-core/
│   ├── images/
│   │   ├── jetson-ota-image.bb
│   │   └── update-bundle.bb
│   └── systemd/
│       └── systemd-serialgetty/
│           └── health-check.service
└── scripts/
    └── jetson-ota.wks
```

### Image Recipe

```bitbake
# recipes-core/images/jetson-ota-image.bb

SUMMARY = "Jetson OTA-enabled image"
LICENSE = "MIT"

inherit core-image

# Enable A/B updates
IMAGE_CLASSES += "image_types_swu"
IMAGE_FSTYPES += "ext4 swu"

# Install OTA components
IMAGE_INSTALL:append = " \
    swupdate \
    swupdate-www \
    update-agent \
    u-boot-fw-utils \
    health-monitor \
"

# Include Docker support
IMAGE_INSTALL:append = " \
    docker \
    docker-compose \
"

# System packages
IMAGE_INSTALL:append = " \
    packagegroup-core-boot \
    packagegroup-core-ssh-openssh \
    kernel-modules \
"

# Read-only root with writable data partition
IMAGE_FEATURES += "read-only-rootfs"

# Persistent data mount
ROOTFS_POSTPROCESS_COMMAND += "setup_data_partition; "

setup_data_partition() {
    # Create mount point
    install -d ${IMAGE_ROOTFS}/data

    # Add to fstab
    echo "/dev/mmcblk0p4 /data ext4 defaults 0 2" >> ${IMAGE_ROOTFS}/etc/fstab

    # Create directories on data partition
    install -d ${IMAGE_ROOTFS}/data/updates
    install -d ${IMAGE_ROOTFS}/data/configs
    install -d ${IMAGE_ROOTFS}/data/models
}

# WIC image with A/B partitions
WKS_FILE = "jetson-ota.wks"
```

### SWUpdate Configuration

```bitbake
# recipes-support/swupdate/swupdate_%.bbappend

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "\
    file://swupdate.cfg \
    file://sw-update-check.service \
"

# Enable features
PACKAGECONFIG:append = " \
    webserver \
    ssl \
    suricatta \
    systemd \
    bootloader \
"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/sw-update-check.service \
        ${D}${systemd_system_unitdir}/

    install -d ${D}${sysconfdir}/swupdate
    install -m 0644 ${WORKDIR}/swupdate.cfg \
        ${D}${sysconfdir}/swupdate/
}

SYSTEMD_SERVICE:${PN} = "sw-update-check.service"
```

### Update Bundle Recipe

```bitbake
# recipes-core/images/update-bundle.bb

SUMMARY = "SWUpdate bundle for OTA"
LICENSE = "MIT"

inherit swupdate

# Base image to include in update
IMAGE_DEPENDS = "jetson-ota-image"

# Update description
SWU_DESCRIPTION = "System update for Jetson devices"
SWU_VERSION = "${DISTRO_VERSION}"

# Signing
SWUPDATE_SIGNING = "RSA"
SWUPDATE_PRIVATE_KEY = "${TOPDIR}/../keys/swupdate-priv.pem"

# Files to include in .swu bundle
SWU_IMAGES = " \
    jetson-ota-image \
    tegra210-p3450-0000 \
"

# sw-description template
python do_swuimage:prepend() {
    description = """
software =
{
    version = "${SWU_VERSION}";

    jetson-nano = {
        hardware-compatibility: ["1.0"];

        stable = {
            images: (
                {
                    filename = "jetson-ota-image-jetson-nano.ext4";
                    type = "raw";
                    device = "/dev/mmcblk0p3";  /* inactive partition */
                    sha256 = "@jetson-ota-image-jetson-nano.ext4.sha256";
                },
                {
                    filename = "tegra210-p3450-0000.dtb";
                    type = "raw";
                    device = "/dev/mmcblk0p3";
                    path = "/boot/devicetree.dtb";
                }
            );

            bootenv: (
                {
                    name = "boot_partition";
                    value = "B";
                },
                {
                    name = "bootcount";
                    value = "0";
                },
                {
                    name = "upgrade_available";
                    value = "1";
                }
            );
        }
    }
}
    """

    with open(d.getVar('WORKDIR') + '/sw-description', 'w') as f:
        f.write(description)
}
```

---

## 7. Fleet Management

### Rollout Strategy

**Phase 1: Canary (1% of fleet, 100 devices)**
- **Duration**: 24 hours
- **Monitoring**: Intensive
- **Rollback**: Automatic on any failure
- **Success Criteria**: 99% success rate, no critical issues

**Phase 2: Pilot (10% of fleet, 1,000 devices)**
- **Duration**: 72 hours
- **Selection**: Diverse geographic/network conditions
- **Success Criteria**: 98% success rate, acceptable issues resolved

**Phase 3: Production (50% of fleet, 5,000 devices)**
- **Duration**: 1 week
- **Waves**: 1,000 devices every 24 hours
- **Success Criteria**: 97% success rate

**Phase 4: Full Deployment (Remaining 40%, 4,000 devices)**
- **Duration**: 1 week
- **Final validation**: All devices updated

### Device Selection Algorithm

```python
def select_canary_devices(fleet, count=100):
    """
    Select diverse device set for canary testing
    """
    devices = []

    # Criteria for canary selection:
    # 1. Different network conditions (WiFi, LTE, 5G)
    # 2. Different geographic regions
    # 3. Different hardware variants (Nano, Xavier)
    # 4. Different workload types
    # 5. Internal test devices

    regions = ['US', 'EU', 'ASIA', 'LATAM']
    network_types = ['wifi', 'lte', '5g']
    hw_variants = ['nano', 'xavier-nx']

    per_combination = count // (len(regions) * len(network_types) * len(hw_variants))

    for region in regions:
        for network in network_types:
            for hw in hw_variants:
                devices.extend(
                    fleet.filter(
                        region=region,
                        network_type=network,
                        hardware=hw
                    ).order_by('?')[:per_combination]
                )

    # Add internal test devices
    devices.extend(fleet.filter(is_test_device=True))

    return devices[:count]
```

### Monitoring Metrics

```yaml
Metrics to Track:
  Update Success:
    - Download success rate
    - Installation success rate
    - Boot success rate
    - Overall success rate (target: >99%)

  Performance:
    - Average download time
    - Average installation time
    - Total update duration
    - Bandwidth usage per device

  Health:
    - Post-update boot count
    - Rollback rate
    - Application startup time
    - AI inference latency

  Operational:
    - Devices online vs offline
    - Update coverage percentage
    - Pending updates count
    - Failed devices requiring attention

Alerting Thresholds:
  - Rollback rate > 1%: Warning
  - Rollback rate > 5%: Critical, pause rollout
  - Success rate < 95%: Investigate
  - Download failures > 10%: Check CDN
```

---

## 8. Security Implementation

### Code Signing Process

```bash
# Build server generates keys (one-time)
openssl genrsa -out swupdate-priv.pem 4096
openssl rsa -in swupdate-priv.pem -out swupdate-pub.pem -outform PEM -pubout

# During build
# 1. Build image
bitbake update-bundle

# 2. Sign .swu file
openssl dgst -sha256 -sign swupdate-priv.pem \
    -out update-bundle.swu.sig \
    tmp/deploy/images/jetson-nano/update-bundle.swu

# 3. Upload to CDN
aws s3 cp update-bundle.swu s3://updates/
aws s3 cp update-bundle.swu.sig s3://updates/
```

### Device Verification

```c
// On device (SWUpdate built with SSL support)
// Public key embedded in image

bool verify_update(const char *swu_file) {
    EVP_PKEY *public_key = load_embedded_public_key();

    // SWUpdate automatically verifies signature
    // using embedded public key

    return swupdate_verify_signature(swu_file, public_key);
}
```

### Network Security

```yaml
Transport Security:
  - All communications over HTTPS (TLS 1.3)
  - Certificate pinning for update server
  - No fallback to HTTP

Authentication:
  - Device certificates (mutual TLS)
  - Device-specific API tokens
  - Rotating credentials (90-day expiry)

Encryption:
  - Updates encrypted in transit (TLS)
  - Optional: Update payload encryption (AES-256)
  - Secure storage of credentials (TPM if available)
```

---

## 9. Network Optimization

### Delta Updates

```bitbake
# Enable delta update generation
inherit swupdate-delta

SWUPDATE_DELTA_SOURCE = "${DEPLOY_DIR_IMAGE}/jetson-ota-image-${PREVIOUS_VERSION}.ext4"
SWUPDATE_DELTA_TARGET = "${DEPLOY_DIR_IMAGE}/jetson-ota-image-${PV}.ext4"

# Uses bsdiff or similar to generate delta
# Typical savings: 80-90% bandwidth for minor updates
```

**Size Comparison:**
- Full update: 2GB
- Delta update (minor): 200MB (90% savings)
- Delta update (major): 800MB (60% savings)

### Resume Support

```python
# Update agent supports HTTP range requests
def download_with_resume(url, local_path):
    if os.path.exists(local_path):
        current_size = os.path.getsize(local_path)
        headers = {'Range': f'bytes={current_size}-'}
        mode = 'ab'  # Append mode
    else:
        current_size = 0
        headers = {}
        mode = 'wb'

    response = requests.get(url, headers=headers, stream=True)

    with open(local_path, mode) as f:
        for chunk in response.iter_content(chunk_size=1024*1024):
            f.write(chunk)
            report_progress(current_size + f.tell())
```

### Bandwidth Management

```yaml
Strategies:
  Scheduling:
    - Updates during off-peak hours (2-6 AM local time)
    - Spread updates across time zones
    - Rate limiting per device (max 5 Mbps)

  Compression:
    - Use XZ compression in .swu bundle
    - Typical compression: 40-50%

  Chunked Transfer:
    - 10MB chunks
    - Parallel downloads if bandwidth allows
    - Pause/resume capability

  CDN Usage:
    - CloudFront edge locations
    - Geographic distribution
    - Caching at ISP level
```

---

## 10. Failure Handling

### Failure Scenarios & Mitigations

| Scenario | Detection | Recovery | Prevention |
|----------|-----------|----------|------------|
| Download interrupted | Checksum mismatch | Resume download | Resumable downloads, retry logic |
| Corrupt download | Signature verification fails | Re-download | Streaming verification |
| Power loss during install | Bootcount > limit | Auto-rollback to A | Atomic writes, A/B partitions |
| Boot failure | U-Boot bootcount | Rollback to previous | Bootloader watchdog |
| Service startup failure | Health check fails | Auto-rollback | Comprehensive health checks |
| Application crash | Systemd restart fails | Manual rollback option | Graceful degradation |
| Storage full | Pre-flight check | Reject update | Space verification before install |
| Network timeout | Download timeout | Retry with exponential backoff | Configurable timeouts |
| Rollback also fails | Both partitions bad | Boot to recovery mode | Recovery partition |

### Recovery Mode

```
Recovery Partition:
  - Minimal rescue system
  - Can re-flash from USB/SD
  - Can download fresh image
  - Manual intervention required

Trigger:
  - Both A and B fail to boot
  - Manual boot menu selection
  - Special GPIO button press
```

---

## 11. Cost Analysis

### Bandwidth Costs

```
Assumptions:
  - 10,000 devices
  - Monthly updates (12 per year)
  - Average update size: 500MB (with delta)
  - Cellular data cost: $0.10/GB

Annual Bandwidth Cost:
  = 10,000 devices × 12 updates × 0.5 GB × $0.10/GB
  = $60,000/year

Optimization with delta updates (vs 2GB full):
  = Savings of $180,000/year (75% reduction)
```

### Infrastructure Costs

```
Monthly Costs:
  - CDN (CloudFront): $2,000
  - S3 storage: $500
  - EC2 fleet manager: $1,000
  - Database (RDS): $500
  - Monitoring: $300
  - Total: $4,300/month = $51,600/year

One-time Costs:
  - Development: $150,000
  - HSM for signing: $5,000
  - Testing infrastructure: $20,000
  - Total: $175,000
```

---

## 12. Success Metrics

### KPIs to Track

```yaml
Reliability:
  - Update success rate: Target >99.5%
  - Rollback rate: Target <0.5%
  - Time to recovery: Target <5 minutes

Performance:
  - Average update duration: Target <2 hours
  - Average downtime: Target <2 minutes
  - Bandwidth per update: Target <600MB

Operational:
  - Fleet coverage: Target 100% within 2 weeks
  - Support tickets: Target <10 per 1000 updates
  - Manual interventions: Target <1%

Business:
  - Cost per update: Target <$0.10
  - Time to deploy security patch: Target <24 hours
  - Compliance: 100% (all devices on approved versions)
```

---

## 13. Risk Mitigation

### Risk Matrix

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Mass rollback event | Critical | Low | Extensive canary testing, gradual rollout |
| CDN outage | High | Medium | Multi-CDN strategy, local caching |
| Key compromise | Critical | Very Low | HSM, key rotation, rapid revocation |
| Bootloader corruption | Critical | Very Low | Protected boot partition, recovery mode |
| Network saturation | Medium | Medium | Rate limiting, time-zone spreading |
| Device bricking | Critical | Very Low | Verified boot, recovery partition |

---

## Summary

This OTA update system design provides:

- Reliable atomic updates with automatic rollback
- Minimal downtime (<2 minutes)
- Scalable to 50,000+ devices
- Cost-effective (delta updates, efficient rollout)
- Secure (signed updates, encrypted transport)
- Operationally mature (monitoring, gradual rollout)

**Total Implementation Time**: 6-8 months with team of 4 engineers
**Ongoing Costs**: ~$112K/year (infrastructure + bandwidth)
**ROI**: Eliminates need for manual updates, enables rapid security patching, supports product evolution
