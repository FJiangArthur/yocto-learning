#!/usr/bin/env python3
"""
recipe_generator.py - Generate BitBake recipes from templates

This tool automates the creation of BitBake recipes for common use cases,
reducing boilerplate and ensuring consistent recipe structure.

Usage:
    python3 recipe_generator.py --type autotools --name myapp --version 1.0
    python3 recipe_generator.py --type cmake --name mylib --version 2.1.3
    python3 recipe_generator.py --type python --name mypkg --version 0.1.0
    python3 recipe_generator.py --type kernel-module --name mydriver --version 1.0
"""

import argparse
import os
import sys
from pathlib import Path
from typing import Dict, Optional
from datetime import datetime


class RecipeTemplate:
    """Base class for recipe templates"""

    def __init__(self, name: str, version: str, license: str = "MIT"):
        self.name = name
        self.version = version
        self.license = license
        self.timestamp = datetime.now().strftime("%Y-%m-%d")

    def generate(self) -> str:
        """Generate the complete recipe content"""
        raise NotImplementedError("Subclasses must implement generate()")

    def get_filename(self) -> str:
        """Get the recipe filename"""
        return f"{self.name}_{self.version}.bb"


class AutotoolsRecipe(RecipeTemplate):
    """Template for autotools-based recipes"""

    def generate(self) -> str:
        return f'''# {self.name}_{self.version}.bb - Autotools-based recipe
# Generated on {self.timestamp}

SUMMARY = "{self.name} application"
DESCRIPTION = "Application built with autotools build system"

LICENSE = "{self.license}"
LIC_FILES_CHKSUM = "file://LICENSE;md5=REPLACE_WITH_ACTUAL_MD5"

HOMEPAGE = "https://github.com/example/{self.name}"
BUGTRACKER = "https://github.com/example/{self.name}/issues"

SECTION = "applications"

SRC_URI = "git://github.com/example/{self.name}.git;protocol=https;branch=main"
SRCREV = "${{AUTOREV}}"

S = "${{WORKDIR}}/git"

inherit autotools pkgconfig

DEPENDS = ""
RDEPENDS:${{PN}} = ""

# Configure options
EXTRA_OECONF = " \\
    --disable-static \\
    --enable-shared \\
"

# Compiler flags
CFLAGS:append = " -O2 -g"

do_install:append() {{
    # Install additional files if needed
    # install -d ${{D}}${{sysconfdir}}/{self.name}
    # install -m 0644 ${{WORKDIR}}/config.conf ${{D}}${{sysconfdir}}/{self.name}/
}}

FILES:${{PN}} = " \\
    ${{bindir}}/{self.name} \\
"

FILES:${{PN}}-dev = " \\
    ${{includedir}} \\
    ${{libdir}}/*.so \\
    ${{libdir}}/pkgconfig \\
"
'''


class CMakeRecipe(RecipeTemplate):
    """Template for CMake-based recipes"""

    def generate(self) -> str:
        return f'''# {self.name}_{self.version}.bb - CMake-based recipe
# Generated on {self.timestamp}

SUMMARY = "{self.name} application"
DESCRIPTION = "Application built with CMake build system"

LICENSE = "{self.license}"
LIC_FILES_CHKSUM = "file://LICENSE;md5=REPLACE_WITH_ACTUAL_MD5"

HOMEPAGE = "https://github.com/example/{self.name}"

SRC_URI = "git://github.com/example/{self.name}.git;protocol=https;branch=main"
SRCREV = "${{AUTOREV}}"

S = "${{WORKDIR}}/git"

inherit cmake pkgconfig

DEPENDS = ""
RDEPENDS:${{PN}} = ""

# CMake configuration
EXTRA_OECMAKE = " \\
    -DCMAKE_BUILD_TYPE=Release \\
    -DBUILD_TESTING=OFF \\
    -DBUILD_SHARED_LIBS=ON \\
"

# Out-of-tree build
B = "${{WORKDIR}}/build"

do_configure:prepend() {{
    # Pre-configuration setup
    echo "#define VERSION \\"${{PV}}\\"" > ${{S}}/include/version.h
}}

do_install:append() {{
    # Post-installation
    install -d ${{D}}${{sysconfdir}}/{self.name}
}}

FILES:${{PN}} = " \\
    ${{bindir}}/{self.name} \\
    ${{sysconfdir}}/{self.name} \\
"
'''


class PythonRecipe(RecipeTemplate):
    """Template for Python package recipes"""

    def generate(self) -> str:
        return f'''# python3-{self.name}_{self.version}.bb - Python package recipe
# Generated on {self.timestamp}

SUMMARY = "Python {self.name} package"
DESCRIPTION = "Python package for {self.name} functionality"

LICENSE = "{self.license}"
LIC_FILES_CHKSUM = "file://LICENSE;md5=REPLACE_WITH_ACTUAL_MD5"

HOMEPAGE = "https://github.com/example/{self.name}"

SRC_URI = "git://github.com/example/{self.name}.git;protocol=https;branch=main"
SRCREV = "${{AUTOREV}}"

S = "${{WORKDIR}}/git"

inherit setuptools3

DEPENDS = " \\
    python3-native \\
    python3-setuptools-native \\
"

RDEPENDS:${{PN}} = " \\
    python3-core \\
"

do_install:append() {{
    # Install additional Python files
    # install -d ${{D}}${{PYTHON_SITEPACKAGES_DIR}}/{self.name}/data
}}

FILES:${{PN}} += " \\
    ${{PYTHON_SITEPACKAGES_DIR}}/{self.name} \\
"
'''

    def get_filename(self) -> str:
        """Python recipes use python3- prefix"""
        return f"python3-{self.name}_{self.version}.bb"


class KernelModuleRecipe(RecipeTemplate):
    """Template for kernel module recipes"""

    def generate(self) -> str:
        return f'''# {self.name}_{self.version}.bb - Kernel module recipe
# Generated on {self.timestamp}

SUMMARY = "{self.name} kernel module"
DESCRIPTION = "Out-of-tree kernel module for {self.name} functionality"

LICENSE = "{self.license}"
LIC_FILES_CHKSUM = "file://COPYING;md5=REPLACE_WITH_ACTUAL_MD5"

SRC_URI = " \\
    file://Makefile \\
    file://{self.name}.c \\
    file://{self.name}.h \\
    file://COPYING \\
"

S = "${{WORKDIR}}"

inherit module

DEPENDS = "virtual/kernel"

KERNEL_MODULE_AUTOLOAD += "{self.name}"
KERNEL_MODULE_PROBECONF += "{self.name}"
module_conf_{self.name} = "options {self.name} debug=1"

COMPATIBLE_MACHINE = "(tegra234|qemux86-64)"
PACKAGE_ARCH = "${{MACHINE_ARCH}}"

do_compile() {{
    oe_runmake \\
        KERNELDIR=${{STAGING_KERNEL_DIR}} \\
        ARCH=${{ARCH}} \\
        CC="${{KERNEL_CC}}" \\
        LD="${{KERNEL_LD}}" \\
        modules
}}

do_install() {{
    install -d ${{D}}${{nonarch_base_libdir}}/modules/${{KERNEL_VERSION}}/extra
    install -m 0644 ${{S}}/{self.name}.ko \\
        ${{D}}${{nonarch_base_libdir}}/modules/${{KERNEL_VERSION}}/extra/
}}

FILES:${{PN}} = " \\
    ${{nonarch_base_libdir}}/modules/${{KERNEL_VERSION}}/extra/{self.name}.ko \\
"
'''


class SystemdServiceRecipe(RecipeTemplate):
    """Template for recipes with systemd services"""

    def generate(self) -> str:
        return f'''# {self.name}_{self.version}.bb - Service with systemd integration
# Generated on {self.timestamp}

SUMMARY = "{self.name} daemon"
DESCRIPTION = "Background service for {self.name}"

LICENSE = "{self.license}"
LIC_FILES_CHKSUM = "file://LICENSE;md5=REPLACE_WITH_ACTUAL_MD5"

SRC_URI = " \\
    git://github.com/example/{self.name}.git;protocol=https;branch=main \\
    file://{self.name}.service \\
"

SRCREV = "${{AUTOREV}}"

S = "${{WORKDIR}}/git"

inherit systemd autotools

DEPENDS = "systemd"
RDEPENDS:${{PN}} = "systemd"

SYSTEMD_SERVICE:${{PN}} = "{self.name}.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install:append() {{
    # Install systemd service file
    install -d ${{D}}${{systemd_system_unitdir}}
    install -m 0644 ${{WORKDIR}}/{self.name}.service \\
        ${{D}}${{systemd_system_unitdir}}/

    # Create runtime directories
    install -d ${{D}}${{localstatedir}}/lib/{self.name}
    install -d ${{D}}${{localstatedir}}/log/{self.name}
}}

FILES:${{PN}} = " \\
    ${{bindir}}/{self.name} \\
    ${{systemd_system_unitdir}}/{self.name}.service \\
    ${{localstatedir}}/lib/{self.name} \\
    ${{localstatedir}}/log/{self.name} \\
"
'''


class RecipeGenerator:
    """Main recipe generator class"""

    TEMPLATES = {
        'autotools': AutotoolsRecipe,
        'cmake': CMakeRecipe,
        'python': PythonRecipe,
        'kernel-module': KernelModuleRecipe,
        'systemd': SystemdServiceRecipe,
    }

    def __init__(self, recipe_type: str, name: str, version: str,
                 license: str = "MIT", output_dir: Optional[Path] = None):
        self.recipe_type = recipe_type
        self.name = name
        self.version = version
        self.license = license
        self.output_dir = output_dir or Path.cwd()

    def generate(self) -> str:
        """Generate recipe content"""
        if self.recipe_type not in self.TEMPLATES:
            raise ValueError(f"Unknown recipe type: {self.recipe_type}")

        template_class = self.TEMPLATES[self.recipe_type]
        template = template_class(self.name, self.version, self.license)
        return template.generate()

    def write_recipe(self) -> Path:
        """Write recipe to file"""
        template_class = self.TEMPLATES[self.recipe_type]
        template = template_class(self.name, self.version, self.license)

        filename = template.get_filename()
        output_path = self.output_dir / filename

        # Create output directory if it doesn't exist
        self.output_dir.mkdir(parents=True, exist_ok=True)

        # Write recipe
        with open(output_path, 'w') as f:
            f.write(template.generate())

        return output_path


def main():
    parser = argparse.ArgumentParser(
        description='Generate BitBake recipes from templates',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog='''
Examples:
  # Generate autotools recipe
  %(prog)s --type autotools --name hello-world --version 1.0

  # Generate CMake recipe
  %(prog)s --type cmake --name myapp --version 2.1 --license Apache-2.0

  # Generate Python package recipe
  %(prog)s --type python --name mypkg --version 0.1.0

  # Generate kernel module recipe
  %(prog)s --type kernel-module --name gpio-driver --version 1.0

  # Generate systemd service recipe
  %(prog)s --type systemd --name mydaemon --version 1.0

  # Specify output directory
  %(prog)s --type cmake --name myapp --version 1.0 --output /path/to/recipes
        '''
    )

    parser.add_argument(
        '--type',
        required=True,
        choices=RecipeGenerator.TEMPLATES.keys(),
        help='Recipe template type'
    )
    parser.add_argument(
        '--name',
        required=True,
        help='Package name'
    )
    parser.add_argument(
        '--version',
        required=True,
        help='Package version'
    )
    parser.add_argument(
        '--license',
        default='MIT',
        help='License (default: MIT)'
    )
    parser.add_argument(
        '--output',
        type=Path,
        help='Output directory (default: current directory)'
    )
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='Print recipe without writing to file'
    )

    args = parser.parse_args()

    try:
        generator = RecipeGenerator(
            recipe_type=args.type,
            name=args.name,
            version=args.version,
            license=args.license,
            output_dir=args.output
        )

        if args.dry_run:
            print(generator.generate())
        else:
            output_path = generator.write_recipe()
            print(f"Generated recipe: {output_path}")
            print(f"\nNext steps:")
            print(f"1. Update LICENSE checksum in {output_path}")
            print(f"2. Adjust SRC_URI to point to actual source")
            print(f"3. Add dependencies (DEPENDS, RDEPENDS)")
            print(f"4. Test with: bitbake {args.name}")

    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
