/*
 * Hello World - Yocto Recipe Example
 *
 * Simple application demonstrating custom recipe creation
 * in the Yocto Project build system.
 *
 * License: MIT
 */

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>

#define VERSION "1.0"

void print_banner(void)
{
    printf("=====================================\n");
    printf("  Hello World - Yocto Recipe Demo\n");
    printf("  Version: %s\n", VERSION);
    printf("=====================================\n\n");
}

void print_system_info(void)
{
    char hostname[256];
    time_t now;

    // Get hostname
    if (gethostname(hostname, sizeof(hostname)) == 0) {
        printf("Running on: %s\n", hostname);
    }

    // Get current time
    time(&now);
    printf("Current time: %s", ctime(&now));
}

int main(int argc, char *argv[])
{
    print_banner();

    printf("Hello from Yocto!\n");
    printf("This is a custom recipe running on NVIDIA Jetson.\n\n");

    print_system_info();

    // Process command-line arguments
    if (argc > 1) {
        printf("\nCommand-line arguments received:\n");
        for (int i = 1; i < argc; i++) {
            printf("  [%d]: %s\n", i, argv[i]);
        }
    } else {
        printf("\nUsage: %s [optional arguments...]\n", argv[0]);
    }

    printf("\nThis application was built using:\n");
    printf("  - Yocto Project BitBake build system\n");
    printf("  - Custom recipe: hello-world_1.0.bb\n");
    printf("  - Cross-compilation for ARM64\n");

    return EXIT_SUCCESS;
}
