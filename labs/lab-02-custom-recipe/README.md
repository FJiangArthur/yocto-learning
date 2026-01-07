# Lab 02: Custom Recipe Development
## Building Your First Application Recipe from Scratch

## Overview

In this hands-on lab, you'll learn to create custom BitBake recipes to build and package your own applications for embedded Linux. You'll start with a simple "Hello World" C application and progress to understanding all aspects of recipe development including source management, compilation, installation, and packaging.

By completing this lab, you'll understand how to integrate custom software into your Yocto-based Linux distribution.

## Learning Objectives

By completing this lab, you will be able to:
- [ ] Create a complete BitBake recipe from scratch
- [ ] Write proper recipe metadata (SUMMARY, DESCRIPTION, LICENSE)
- [ ] Manage source files with SRC_URI
- [ ] Implement do_compile and do_install tasks
- [ ] Package applications correctly with FILES variable
- [ ] Add recipes to images using IMAGE_INSTALL
- [ ] Test custom applications on target or QEMU
- [ ] Debug recipe build failures
- [ ] Follow BitBake recipe best practices

## Prerequisites

- [ ] Completed Lab 01 (First Yocto Build)
- [ ] Working Yocto build environment
- [ ] Successfully built core-image-minimal
- [ ] Basic C programming knowledge
- [ ] Understanding of Makefiles
- [ ] Text editor (vim, nano, or VS Code)
- [ ] Jetson device or QEMU for testing

## Time Estimate

**Total:** 3-4 hours

Breakdown:
- Recipe basics: 45 minutes
- Source code creation: 30 minutes
- Recipe development: 1 hour
- Building and testing: 45 minutes
- Advanced features: 30-45 minutes
- Verification: 15 minutes

## Materials Needed

- Completed Lab 01 workspace
- Access to build environment
- Approximately 2GB additional disk space for this lab

## What You'll Build

1. **hello-world application** - Simple C program demonstrating recipe basics
2. **Complete BitBake recipe** - Proper metadata and build instructions
3. **Makefile** - Build system for the application
4. **Custom image** - Image including your application

## Lab Structure

This lab includes:
- **INSTRUCTIONS.md** - Step-by-step guide through recipe creation
- **starter/** - Skeleton files to get you started
- **solution/** - Complete working solution for reference
- **VERIFICATION.md** - How to verify successful completion

## Key Concepts

You'll learn about:
- **Recipe variables:** SUMMARY, LICENSE, SRC_URI, S, D, B, WORKDIR
- **Tasks:** do_fetch, do_compile, do_install, do_package
- **Packaging:** FILES, PACKAGES, RDEPENDS
- **Integration:** Adding recipes to images

## Ready to Begin?

Proceed to INSTRUCTIONS.md for detailed guidance.

---

**Lab Version:** 1.0
**Last Updated:** 2025-12-31
**Tested With:** Yocto Kirkstone (4.0)
