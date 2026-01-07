# Problem 01: Basic Recipe - Hello World

## Difficulty: Junior
## Time Limit: 30 minutes

## Problem Statement

Create a complete BitBake recipe from scratch for a simple "Hello World" C application. This tests your understanding of basic recipe structure, compilation, and installation.

## Background

You are given a simple C application source code. Your task is to write a BitBake recipe that:
1. Fetches the source code
2. Compiles it
3. Installs the binary to the correct location
4. Packages it properly

## Requirements

- [ ] Create a proper recipe file with all required metadata (SUMMARY, LICENSE, etc.)
- [ ] Use appropriate LICENSE and LIC_FILES_CHKSUM
- [ ] Compile the C source file
- [ ] Install the binary to ${bindir}
- [ ] Ensure the recipe builds without errors
- [ ] Follow BitBake naming conventions

## Input/Starting Files

**Source Code (hello.c):**
```c
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[]) {
    printf("Hello from Yocto!\n");
    printf("Version: 1.0\n");
    return 0;
}
```

**License File (COPYING):**
```
MIT License

Copyright (c) 2024 Yocto Interview

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Expected Output

1. A working recipe file: `hello_1.0.bb`
2. The recipe should build successfully with `bitbake hello`
3. The resulting package should contain:
   - Binary at `/usr/bin/hello`
   - Proper package metadata
4. Running the binary should output:
   ```
   Hello from Yocto!
   Version: 1.0
   ```

## Evaluation Criteria

| Criteria | Weight | Points |
|----------|--------|--------|
| Recipe compiles without errors | 30% | 30 |
| Proper metadata (LICENSE, SUMMARY, etc.) | 20% | 20 |
| Correct installation path | 20% | 20 |
| Follows BitBake conventions | 15% | 15 |
| Clean and readable code | 10% | 10 |
| Proper file organization | 5% | 5 |
| **Total** | **100%** | **100** |

## Constraints

- Use only standard BitBake features (no custom classes)
- The source files should be placed in `files/` directory relative to the recipe
- Use `file://` URI for local sources
- Follow standard Yocto directory structure

## Hints

1. What directory structure do you need for storing local source files?
2. How do you calculate the MD5 checksum for the license file?
3. What BitBake variables control where files are installed?
4. Do you need any special classes for a simple C application?

## Submission Format

Submit a directory structure like this:
```
hello/
├── hello_1.0.bb
└── files/
    ├── hello.c
    └── COPYING
```

## Common Mistakes to Avoid

- Forgetting LIC_FILES_CHKSUM
- Installing to wrong directory (not using ${bindir})
- Not specifying SRC_URI correctly for local files
- Missing do_install task
- Hardcoding paths instead of using BitBake variables

## Success Criteria

- [ ] Recipe parses without errors: `bitbake -p`
- [ ] Recipe builds successfully: `bitbake hello`
- [ ] Binary exists in tmp/work/.../image/usr/bin/hello
- [ ] Package created: hello-1.0-r0.rpm (or .ipk, .deb)
- [ ] Binary executes correctly on target
