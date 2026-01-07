# Makefile for Hello World - Yocto Recipe Example
#
# This Makefile supports cross-compilation through the CROSS_COMPILE
# variable, which is automatically set by the Yocto build environment.

# Compiler and flags
CC = $(CROSS_COMPILE)gcc
CFLAGS = -Wall -Wextra -O2 -std=c99
LDFLAGS =

# Target binary name
TARGET = hello-world

# Source files
SOURCES = hello-world.c
OBJECTS = $(SOURCES:.c=.o)

# Installation directories (set by Yocto)
DESTDIR ?=
bindir ?= /usr/bin

# Default target
all: $(TARGET)

# Build the target binary
$(TARGET): $(OBJECTS)
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $^
	@echo "Build complete: $(TARGET)"

# Compile source files to object files
%.o: %.c
	$(CC) $(CFLAGS) -c -o $@ $<

# Install target (used by do_install)
install: $(TARGET)
	install -d $(DESTDIR)$(bindir)
	install -m 0755 $(TARGET) $(DESTDIR)$(bindir)/$(TARGET)
	@echo "Installed $(TARGET) to $(DESTDIR)$(bindir)"

# Clean build artifacts
clean:
	rm -f $(TARGET) $(OBJECTS)
	@echo "Clean complete"

# Clean everything including dependencies
distclean: clean
	@echo "Distclean complete"

# Phony targets
.PHONY: all install clean distclean

# Help target
help:
	@echo "Available targets:"
	@echo "  all       - Build the application (default)"
	@echo "  install   - Install to DESTDIR/bindir"
	@echo "  clean     - Remove built files"
	@echo "  distclean - Remove all generated files"
	@echo "  help      - Show this help message"
	@echo ""
	@echo "Variables:"
	@echo "  CC        = $(CC)"
	@echo "  CFLAGS    = $(CFLAGS)"
	@echo "  DESTDIR   = $(DESTDIR)"
	@echo "  bindir    = $(bindir)"
