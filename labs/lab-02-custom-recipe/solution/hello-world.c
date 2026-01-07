#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/utsname.h>

#define VERSION "1.0"

void print_banner(void) {
    printf("================================\n");
    printf("  Hello World - Yocto Edition  \n");
    printf("  Version: %s                  \n", VERSION);
    printf("================================\n\n");
}

void print_system_info(void) {
    struct utsname sys_info;
    if (uname(&sys_info) == 0) {
        printf("System Information:\n");
        printf("  OS Name:    %s\n", sys_info.sysname);
        printf("  Hostname:   %s\n", sys_info.nodename);
        printf("  Kernel:     %s\n", sys_info.release);
        printf("  Arch:       %s\n", sys_info.machine);
        printf("\n");
    }
}

int main(int argc, char *argv[]) {
    print_banner();
    print_system_info();
    printf("This application was built with Yocto Project!\n");
    printf("Build Date: %s %s\n", __DATE__, __TIME__);
    printf("\n");
    return 0;
}
