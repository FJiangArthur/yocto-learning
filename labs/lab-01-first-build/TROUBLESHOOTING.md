# Lab 01: Troubleshooting Guide

## Quick Reference

| Error Type | Common Cause | Quick Fix |
|------------|--------------|-----------|
| Package not found | Missing dependencies | Install build essentials |
| Disk space error | Insufficient space | Clear 50GB+ |
| Network timeout | Slow/unstable connection | Retry with BB_FETCH_PREMIRRORONLY |
| Task failed | Corrupted download | Clean and rebuild |
| Python error | Wrong Python version | Use Python 3.8+ |
| Locale error | Missing UTF-8 locale | Set LC_ALL=en_US.UTF-8 |

---

## Common Errors and Solutions

### Error 1: Missing Build Dependencies

**Symptom:**
```
ERROR: Please install the following missing utilities: chrpath, diffstat
```

**Cause:** Required host packages not installed.

**Solution:**
```bash
sudo apt update
sudo apt install -y gawk wget git diffstat unzip texinfo gcc build-essential \
chrpath socat cpio python3 python3-pip python3-pexpect xz-utils debianutils \
iputils-ping python3-git python3-jinja2 python3-subunit zstd liblz4-tool
```

**Verification:**
```bash
which chrpath diffstat gawk
```

---

### Error 2: Insufficient Disk Space

**Symptom:**
```
ERROR: No new tasks can be executed since the disk space monitor action is "STOPTASKS"!
ERROR: Immediately halt since the disk space monitor action is "HALT"!
```

**Cause:** Build requires 50GB+ free space.

**Solution:**

**Check current usage:**
```bash
df -h ~/yocto-labs/lab-01/
```

**Free up space:**
```bash
# Remove previous build artifacts
cd ~/yocto-labs/lab-01/poky/build-minimal
rm -rf tmp/

# Clear package manager cache
sudo apt clean
sudo apt autoclean

# Find large files
du -sh * | sort -rh | head -10
```

**Adjust disk monitoring (if space is tight):**
Edit `conf/local.conf` and modify:
```
BB_DISKMON_DIRS = "\
    STOPTASKS,${TMPDIR},500M,100K \
    HALT,${TMPDIR},100M,1K"
```

---

### Error 3: Network/Download Failures

**Symptom:**
```
WARNING: Failed to fetch URL http://downloads.sourceforge.net/...
ERROR: Fetcher failure: Unable to find revision ...
```

**Cause:** Network timeout, server down, or firewall blocking.

**Solution 1: Retry the build**
```bash
bitbake core-image-minimal
```
BitBake will resume and retry failed downloads.

**Solution 2: Use mirror sources**
Add to `conf/local.conf`:
```
SOURCE_MIRROR_URL ?= "http://downloads.yoctoproject.org/mirror/sources/"
INHERIT += "own-mirrors"
BB_GENERATE_MIRROR_TARBALLS = "1"
```

**Solution 3: Increase network timeouts**
Add to `conf/local.conf`:
```
BB_FETCH_TIMEOUT = "60"
```

**Solution 4: Check proxy settings**
If behind corporate firewall:
```bash
export http_proxy="http://proxy.company.com:8080"
export https_proxy="http://proxy.company.com:8080"
export no_proxy="localhost,127.0.0.1"
```

---

### Error 4: Git Clone Failures

**Symptom:**
```
ERROR: Fetcher failure for URL: 'git://git.example.com/repo.git'
fatal: unable to connect to git.example.com
```

**Cause:** Git protocol blocked by firewall.

**Solution:**
Configure Git to use HTTPS instead of git://
```bash
git config --global url."https://".insteadOf git://
```

Add to `conf/local.conf`:
```
BB_GIT_SHALLOW = "1"
BB_GIT_SHALLOW_DEPTH = "1"
```

---

### Error 5: Task Failure - do_compile

**Symptom:**
```
ERROR: Task (/path/to/recipe_1.0.bb:do_compile) failed with exit code '1'
```

**Cause:** Compilation error in a specific package.

**Solution:**

**1. View detailed log:**
```bash
# Find the log file path in error message, usually:
cat tmp/work/core2-64-poky-linux/PACKAGE/VERSION/temp/log.do_compile
```

**2. Clean and rebuild single package:**
```bash
bitbake -c cleanall PACKAGE_NAME
bitbake PACKAGE_NAME
```

**3. If persistent, check for known issues:**
```bash
# Search online for the specific error
# Check Yocto bugzilla: https://bugzilla.yoctoproject.org/
```

**4. Build with verbose output:**
```bash
bitbake -v core-image-minimal
```

---

### Error 6: Locale/Encoding Errors

**Symptom:**
```
ERROR: UnicodeDecodeError: 'ascii' codec can't decode byte
ERROR: Please use a locale setting which supports UTF-8
```

**Cause:** System locale not set to UTF-8.

**Solution:**
```bash
sudo locale-gen en_US.UTF-8
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8
echo 'export LC_ALL=en_US.UTF-8' >> ~/.bashrc
echo 'export LANG=en_US.UTF-8' >> ~/.bashrc
source ~/.bashrc
```

**Verification:**
```bash
locale
# Should show LC_ALL=en_US.UTF-8
```

---

### Error 7: Python Version Mismatch

**Symptom:**
```
ERROR: Python 3.6 or newer is required
ERROR: BitBake requires Python 3.8+
```

**Cause:** System Python too old.

**Solution:**

**Check Python version:**
```bash
python3 --version
```

**Install Python 3.8+ (Ubuntu 18.04):**
```bash
sudo apt install software-properties-common
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt update
sudo apt install python3.8 python3.8-dev
sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.8 1
```

**Verify:**
```bash
python3 --version
# Should show Python 3.8.x or newer
```

---

### Error 8: Shared State (sstate) Corruption

**Symptom:**
```
ERROR: Sstate package corruption detected
WARNING: Removing sstate cache file
```

**Cause:** Corrupted shared state cache, usually from interrupted builds.

**Solution:**

**Clear sstate cache:**
```bash
cd ~/yocto-labs/lab-01/poky/build-minimal
rm -rf sstate-cache/
```

**Clear specific package sstate:**
```bash
bitbake -c cleansstate PACKAGE_NAME
```

**Rebuild:**
```bash
bitbake core-image-minimal
```

---

### Error 9: TMPDIR Permission Issues

**Symptom:**
```
ERROR: Unable to create file /path/to/tmp/...
ERROR: Permission denied
```

**Cause:** Incorrect permissions on build directories.

**Solution:**
```bash
cd ~/yocto-labs/lab-01/poky/build-minimal
sudo chown -R $USER:$USER tmp/
chmod -R u+w tmp/
```

**Prevention:** Never run BitBake with sudo.

---

### Error 10: Hash Equivalence Server Issues

**Symptom:**
```
WARNING: Hash Equivalence Server connection timeout
ERROR: Unable to connect to hash equivalence server
```

**Cause:** Hash equivalence server unreachable or not needed.

**Solution:**
Disable hash equivalence in `conf/local.conf`:
```
BB_HASHSERVE = ""
BB_SIGNATURE_HANDLER = "OEBasicHash"
```

Or use local hash equivalence:
```
BB_HASHSERVE = "auto"
BB_HASHSERVE_UPSTREAM = ""
```

---

### Error 11: Out of Memory (OOM)

**Symptom:**
```
ERROR: gcc: fatal error: Killed signal terminated program
virtual memory exhausted: Cannot allocate memory
```

**Cause:** Insufficient RAM for parallel compilation.

**Solution:**

**Reduce parallel tasks in `conf/local.conf`:**
```
BB_NUMBER_THREADS = "4"
PARALLEL_MAKE = "-j 4"
```

**Add swap space (temporary fix):**
```bash
sudo fallocate -l 8G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

**Check memory usage during build:**
```bash
watch -n 5 free -h
```

---

### Error 12: QA Issues (Warnings)

**Symptom:**
```
WARNING: QA Issue: package contains .la files [installed-vs-shipped]
WARNING: QA Issue: No GNU_HASH in the ELF binary
```

**Cause:** Quality assurance checks found potential issues.

**Impact:** Usually non-critical, build will complete.

**Solution (if blocking):**
Add to `conf/local.conf` to make warnings non-fatal:
```
WARN_QA:remove = "installed-vs-shipped"
```

Or fix the recipe if you're developing custom recipes (covered in Lab 02).

---

### Error 13: Missing Layer Dependencies

**Symptom:**
```
ERROR: Layer 'meta-example' depends upon layer 'meta-other', but we can't find it
```

**Cause:** bblayers.conf missing required layers.

**Solution:**

**Check layer dependencies:**
```bash
bitbake-layers show-layers
```

**Add missing layer to conf/bblayers.conf:**
```
BBLAYERS ?= " \
  /path/to/poky/meta \
  /path/to/poky/meta-poky \
  /path/to/poky/meta-yocto-bsp \
  /path/to/meta-other \
  "
```

---

### Error 14: Build Hangs/Frozen

**Symptom:**
- No output for 30+ minutes
- Same task stuck indefinitely

**Cause:** Deadlock, network timeout, or resource starvation.

**Solution:**

**1. Check if still running:**
```bash
top
# Look for high CPU/memory processes
ps aux | grep bitbake
```

**2. Enable progress logging:**
In another terminal:
```bash
tail -f tmp/log/cooker/qemux86-64/console-latest.log
```

**3. If truly hung, kill and restart:**
```bash
killall bitbake
bitbake core-image-minimal
```

**4. Reduce parallelism:**
```
BB_NUMBER_THREADS = "2"
PARALLEL_MAKE = "-j 2"
```

---

### Error 15: Version Mismatch (SANITY CHECK)

**Symptom:**
```
ERROR: Bitbake version 1.46 is required and version 1.44 was found
ERROR: Sanity check failed
```

**Cause:** Incompatible BitBake/Poky versions.

**Solution:**

**Ensure correct branch:**
```bash
cd ~/yocto-labs/lab-01/poky
git branch
# Should show * kirkstone
```

**If on wrong branch:**
```bash
git checkout kirkstone
git pull
```

**Clean and rebuild:**
```bash
cd build-minimal
rm -rf tmp/
bitbake core-image-minimal
```

---

## General Debugging Tips

### Enable Detailed Logging

```bash
bitbake -v -D core-image-minimal
```
- `-v`: Verbose output
- `-D`: Debug output

### View Task Dependencies

```bash
bitbake -g core-image-minimal
# Generates task-depends.dot
```

### List All Recipes

```bash
bitbake-layers show-recipes
```

### Show Recipe Information

```bash
bitbake -s | grep PACKAGE_NAME
bitbake -e PACKAGE_NAME | less
```

### Force Rebuild

```bash
# Clean single package
bitbake -c clean PACKAGE_NAME

# Clean all (remove from cache)
bitbake -c cleanall PACKAGE_NAME

# Clean entire build
rm -rf tmp/
```

### Check Configuration

```bash
bitbake -e core-image-minimal | grep ^MACHINE=
bitbake -e core-image-minimal | grep ^DISTRO=
```

---

## Advanced Debugging

### Drop to DevShell

```bash
bitbake -c devshell PACKAGE_NAME
```
Opens a shell with build environment configured. You can manually run compile commands.

### Examine Work Directory

```bash
cd tmp/work/core2-64-poky-linux/PACKAGE/VERSION/
ls
# Shows all build artifacts, logs, and temporary files
```

### Log File Locations

```
tmp/log/cooker/          # BitBake cooker logs
tmp/work/.../temp/       # Per-task logs
tmp/log/error-report/    # Error reports
```

---

## When to Ask for Help

If after trying these solutions you still have issues:

1. **Gather information:**
   - Error message (complete)
   - Build configuration (local.conf, bblayers.conf)
   - Build log (tmp/log/cooker/...)
   - System info (OS, disk space, RAM)

2. **Search existing resources:**
   - Yocto mailing list archives
   - Stack Overflow
   - Yocto Bugzilla

3. **Ask on community channels:**
   - Yocto Project mailing list
   - #yocto IRC channel on Libera.Chat
   - Yocto Project Discord

4. **Provide context:**
   - What you're trying to do
   - What you've already tried
   - Complete error messages
   - Relevant configuration

---

## Prevention Best Practices

1. **Start clean:** Begin with fresh Ubuntu installation or VM
2. **Check requirements:** Verify disk space, RAM, internet before starting
3. **Use stable releases:** Stick to LTS releases (kirkstone, dunfell)
4. **Don't modify during build:** Let first build complete before experimenting
5. **Backup config:** Always backup local.conf before changes
6. **Read error messages:** Don't skip past errors, understand them
7. **One change at a time:** Make incremental changes, not massive rewrites
8. **Use version control:** Git commit working configurations

---

## Quick Recovery Checklist

Build failed? Try in order:

- [ ] Read error message completely
- [ ] Check disk space (`df -h`)
- [ ] Check internet connection
- [ ] Verify locale (`echo $LC_ALL`)
- [ ] Clean and retry (`bitbake -c clean PACKAGE; bitbake PACKAGE`)
- [ ] Check log files in tmp/work/.../temp/
- [ ] Search error message online
- [ ] Clear sstate cache
- [ ] Start fresh build (rm -rf tmp/)
- [ ] Ask community for help

---

**Most issues are resolved by:**
1. Ensuring adequate disk space
2. Stable internet connection
3. Correct locale settings
4. Using supported OS versions
5. Not interrupting builds

**Remember:** The first build is always the hardest. Once successful, rebuilds are much faster and more reliable!
