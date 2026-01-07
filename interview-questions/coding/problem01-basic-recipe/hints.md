# Hints for Problem 01: Basic Recipe

## Progressive Hints

Use these hints if you get stuck. Try to solve as much as possible before looking at each hint.

---

### Hint 1: Recipe File Structure (Difficulty: 0/5)

<details>
<summary>Click to reveal</summary>

A basic BitBake recipe needs these essential components:

```bitbake
SUMMARY = "Short description"
DESCRIPTION = "Longer description"
LICENSE = "License-Name"
LIC_FILES_CHKSUM = "file://LICENSE-FILE;md5=CHECKSUM"

SRC_URI = "where to get source"

# Build instructions
do_compile() {
    # compilation commands
}

do_install() {
    # installation commands
}
```

</details>

---

### Hint 2: Directory Structure (Difficulty: 1/5)

<details>
<summary>Click to reveal</summary>

For local source files, create this structure:

```
meta-custom/recipes-example/hello/
├── hello_1.0.bb          # Recipe file
└── files/                # Local files directory
    ├── hello.c           # Source code
    └── COPYING           # License file
```

BitBake automatically searches in `files/` directory for `file://` URIs.

</details>

---

### Hint 3: License Checksum (Difficulty: 2/5)

<details>
<summary>Click to reveal</summary>

To calculate the MD5 checksum for your license file:

```bash
md5sum files/COPYING
```

Or on macOS:
```bash
md5 files/COPYING
```

Use the output in your LIC_FILES_CHKSUM like this:
```bitbake
LIC_FILES_CHKSUM = "file://COPYING;md5=<calculated-md5-here>"
```

</details>

---

### Hint 4: SRC_URI for Local Files (Difficulty: 2/5)

<details>
<summary>Click to reveal</summary>

For local files in the `files/` directory, use the `file://` protocol:

```bitbake
SRC_URI = "file://hello.c \
           file://COPYING \
          "
```

BitBake will look for these files in:
1. `${PN}/` directory (recipe-name directory)
2. `files/` subdirectory

</details>

---

### Hint 5: Setting the Source Directory (Difficulty: 2/5)

<details>
<summary>Click to reveal</summary>

Since you're using local files (not a tarball), you need to set where the source is:

```bitbake
S = "${WORKDIR}"
```

After `do_unpack`, your files will be in `${WORKDIR}`, so your source directory should point there.

</details>

---

### Hint 6: Compilation Task (Difficulty: 3/5)

<details>
<summary>Click to reveal</summary>

For a simple C file, you can compile directly in `do_compile`:

```bitbake
do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} hello.c -o hello
}
```

Important variables:
- `${CC}` - Cross-compiler (set by BitBake)
- `${CFLAGS}` - Compiler flags
- `${LDFLAGS}` - Linker flags

Never use `gcc` directly - always use `${CC}` for cross-compilation!

</details>

---

### Hint 7: Installation Task (Difficulty: 3/5)

<details>
<summary>Click to reveal</summary>

Install the binary to the correct system location:

```bitbake
do_install() {
    install -d ${D}${bindir}
    install -m 0755 hello ${D}${bindir}/
}
```

Key points:
- `install -d` creates the directory
- `${D}` is the destination root
- `${bindir}` typically expands to `/usr/bin`
- `-m 0755` sets executable permissions

</details>

---

### Hint 8: Complete Recipe Template (Difficulty: 4/5)

<details>
<summary>Click to reveal</summary>

Here's the basic structure you should fill in:

```bitbake
SUMMARY = "???"
DESCRIPTION = "???"
LICENSE = "???"
LIC_FILES_CHKSUM = "file://???;md5=???"

SRC_URI = "???"

S = "???"

do_compile() {
    # Compile command here
}

do_install() {
    # Install commands here
}
```

Fill in the ??? parts based on the problem requirements.

</details>

---

### Hint 9: Testing Your Recipe (Difficulty: 4/5)

<details>
<summary>Click to reveal</summary>

Test your recipe step by step:

```bash
# 1. Check syntax
bitbake -p

# 2. Parse your recipe
bitbake -e hello | grep "^SUMMARY="

# 3. Try compilation only
bitbake -c compile hello

# 4. Try installation only
bitbake -c install hello

# 5. Full build
bitbake hello

# 6. Check installed files
ls tmp/work/*/hello/1.0-r0/image/usr/bin/

# 7. Enter devshell for debugging
bitbake -c devshell hello
```

</details>

---

### Hint 10: Common Errors and Solutions (Difficulty: 5/5)

<details>
<summary>Click to reveal</summary>

**Error: "LIC_FILES_CHKSUM mismatch"**
- Solution: Recalculate MD5 of your COPYING file

**Error: "No such file hello.c"**
- Solution: Check that files are in `files/` directory
- Verify SRC_URI uses `file://` protocol

**Error: "oe_runmake failed"**
- Solution: You don't need Make for this simple recipe
- Use direct compilation in do_compile()

**Error: "QA Issue: File ... in package but not in FILES"**
- Solution: Check that you're installing to ${D}${bindir}, not just ${bindir}

**Error: "Binary doesn't execute on target"**
- Solution: Make sure you used ${CC} not `gcc`
- Cross-compilation requires proper toolchain

</details>

---

## Debugging Tips

1. **Use devshell**: `bitbake -c devshell hello`
   - Inspect variables: `echo $S`, `echo $D`, `echo $CC`
   - Try manual compilation

2. **Check variables**: `bitbake -e hello | grep "^VARIABLE="`
   - Verify SRC_URI expanded correctly
   - Check S, B, D paths

3. **Read logs**: `tmp/work/*/hello/1.0-r0/temp/log.do_compile.*`

4. **Incremental testing**: Test each task individually
   - `bitbake -c fetch hello`
   - `bitbake -c unpack hello`
   - `bitbake -c compile hello`
   - `bitbake -c install hello`

## Need More Help?

If you've tried all hints and still stuck:
1. Review the example recipes in `meta/recipes-extended/hello/`
2. Check the solution file (but try all hints first!)
3. Ask specific questions about what's failing
