# yocto-utils_1.0.bb - Utility scripts for Yocto development
#
# Collection of helper scripts for common Yocto development tasks
# Including build analysis, dependency tracking, and troubleshooting tools

SUMMARY = "Yocto development utilities and helper scripts"
DESCRIPTION = "Collection of command-line tools to simplify Yocto development \
workflow, including build analysis, layer management, and debugging helpers."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://yocto-analyze-build.sh \
    file://yocto-find-recipe.sh \
    file://yocto-list-tasks.sh \
    file://yocto-show-config.sh \
    file://yocto-clean-build.sh \
"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${bindir}

    # Install utility scripts
    install -m 0755 ${WORKDIR}/yocto-analyze-build.sh ${D}${bindir}/yocto-analyze-build
    install -m 0755 ${WORKDIR}/yocto-find-recipe.sh ${D}${bindir}/yocto-find-recipe
    install -m 0755 ${WORKDIR}/yocto-list-tasks.sh ${D}${bindir}/yocto-list-tasks
    install -m 0755 ${WORKDIR}/yocto-show-config.sh ${D}${bindir}/yocto-show-config
    install -m 0755 ${WORKDIR}/yocto-clean-build.sh ${D}${bindir}/yocto-clean-build
}

FILES:${PN} = "${bindir}/*"

RDEPENDS:${PN} = "bash"
