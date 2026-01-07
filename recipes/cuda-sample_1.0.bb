# cuda-sample_1.0.bb - CUDA application recipe for Jetson
#
# This recipe demonstrates:
# - CUDA toolkit integration in Yocto
# - GPU-accelerated computing on embedded platforms
# - CUDA cross-compilation setup
# - Runtime CUDA library dependencies
# - Performance optimization for embedded GPUs
#
# Learning objectives:
# 1. Understand CUDA SDK dependencies in Yocto
# 2. Learn cross-compilation for NVIDIA GPU code
# 3. Practice GPU memory management for embedded systems
# 4. Handle CUDA compute capability targeting

SUMMARY = "CUDA-accelerated sample application for Jetson"
DESCRIPTION = "Educational CUDA example demonstrating GPU programming on \
NVIDIA Jetson platforms. Includes vector addition, matrix operations, and \
performance benchmarking optimized for embedded GPU architectures."

HOMEPAGE = "https://github.com/example/jetson-cuda-sample"
SECTION = "gpu"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=34400b68072d710fecd0a2940a0d1658"

SRC_URI = " \
    git://github.com/example/cuda-sample.git;protocol=https;branch=main \
    file://cuda-sample.conf \
"

SRCREV = "0123456789abcdef0123456789abcdef01234567"

S = "${WORKDIR}/git"

# Build dependencies
# cuda-toolkit: CUDA compiler (nvcc) and development headers
# cudnn: CUDA Deep Neural Network library (optional, for DNN operations)
DEPENDS = " \
    cuda-toolkit \
    cudnn \
"

# Runtime dependencies
# cuda-libraries: CUDA runtime libraries (libcudart.so, etc.)
# nvidia-l4t-core: NVIDIA L4T core packages for Jetson
RDEPENDS:${PN} = " \
    cuda-libraries \
    libcudart \
    libcublas \
    libcufft \
"

# Only compatible with Jetson platforms (have CUDA support)
COMPATIBLE_MACHINE = "(tegra234|jetson-orin.*|jetson-xavier.*)"

# Machine-specific due to GPU architecture differences
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit cuda

# CUDA architecture - Jetson Orin has Ampere (sm_87)
# sm_87: Orin, sm_72: Xavier, sm_53: TX2, sm_62: TX2i/Nano
CUDA_ARCH = "sm_87"

# For multiple architectures (increases binary size):
# CUDA_ARCH = "sm_72,sm_87"

# CUDA compiler flags
CUDA_NVCC_FLAGS = " \
    -arch=${CUDA_ARCH} \
    --compiler-options '-fPIC -O3' \
    --ptxas-options=-v \
    --use_fast_math \
    -lineinfo \
"

# C++ compiler flags for CUDA host code
CXXFLAGS:append = " -std=c++14 -O3 -march=armv8-a+crypto"

# Linker flags for CUDA libraries
LDFLAGS:append = " \
    -L${STAGING_LIBDIR}/cuda \
    -lcudart \
    -lcublas \
    -lcufft \
"

# Environment for CUDA cross-compilation
export CUDA_PATH = "${STAGING_DIR_HOST}/usr/local/cuda"
export CUDA_TOOLKIT_ROOT_DIR = "${CUDA_PATH}"
export CUDA_INCLUDE_DIRS = "${CUDA_PATH}/include"

do_compile() {
    # Compile CUDA source files
    cd ${S}

    # Example: Compile vector addition kernel
    ${CUDA_PATH}/bin/nvcc ${CUDA_NVCC_FLAGS} \
        -I${CUDA_INCLUDE_DIRS} \
        -c src/vector_add.cu -o vector_add.o

    # Example: Compile matrix multiplication kernel
    ${CUDA_PATH}/bin/nvcc ${CUDA_NVCC_FLAGS} \
        -I${CUDA_INCLUDE_DIRS} \
        -c src/matrix_mul.cu -o matrix_mul.o

    # Compile host code
    ${CXX} ${CXXFLAGS} \
        -I${CUDA_INCLUDE_DIRS} \
        -c src/main.cpp -o main.o

    # Link everything together
    ${CXX} ${LDFLAGS} \
        main.o vector_add.o matrix_mul.o \
        ${LDFLAGS} \
        -o cuda-sample
}

do_install() {
    # Install CUDA binary
    install -d ${D}${bindir}
    install -m 0755 ${S}/cuda-sample ${D}${bindir}/

    # Install configuration
    install -d ${D}${sysconfdir}/cuda-sample
    install -m 0644 ${WORKDIR}/cuda-sample.conf \
        ${D}${sysconfdir}/cuda-sample/

    # Install sample kernels source (for reference/modification)
    install -d ${D}${datadir}/cuda-sample/kernels
    install -m 0644 ${S}/src/*.cu ${D}${datadir}/cuda-sample/kernels/

    # Install benchmark scripts
    install -d ${D}${datadir}/cuda-sample/benchmarks
    if [ -d ${S}/benchmarks ]; then
        install -m 0755 ${S}/benchmarks/*.sh ${D}${datadir}/cuda-sample/benchmarks/
    fi
}

FILES:${PN} = " \
    ${bindir}/cuda-sample \
    ${sysconfdir}/cuda-sample \
    ${datadir}/cuda-sample \
"

# Example CUDA kernel (vector_add.cu):
#
# #include <cuda_runtime.h>
# #include <stdio.h>
#
# // CUDA kernel for vector addition
# __global__ void vectorAdd(const float *A, const float *B, float *C, int N) {
#     int idx = blockDim.x * blockIdx.x + threadIdx.x;
#     if (idx < N) {
#         C[idx] = A[idx] + B[idx];
#     }
# }
#
# extern "C" void launchVectorAdd(const float *h_A, const float *h_B,
#                                  float *h_C, int N) {
#     float *d_A, *d_B, *d_C;
#     size_t size = N * sizeof(float);
#
#     // Allocate device memory
#     cudaMalloc((void**)&d_A, size);
#     cudaMalloc((void**)&d_B, size);
#     cudaMalloc((void**)&d_C, size);
#
#     // Copy data to device
#     cudaMemcpy(d_A, h_A, size, cudaMemcpyHostToDevice);
#     cudaMemcpy(d_B, h_B, size, cudaMemcpyHostToDevice);
#
#     // Launch kernel
#     int threadsPerBlock = 256;
#     int blocksPerGrid = (N + threadsPerBlock - 1) / threadsPerBlock;
#     vectorAdd<<<blocksPerGrid, threadsPerBlock>>>(d_A, d_B, d_C, N);
#
#     // Copy result back to host
#     cudaMemcpy(h_C, d_C, size, cudaMemcpyDeviceToHost);
#
#     // Free device memory
#     cudaFree(d_A);
#     cudaFree(d_B);
#     cudaFree(d_C);
#
#     // Check for errors
#     cudaError_t err = cudaGetLastError();
#     if (err != cudaSuccess) {
#         fprintf(stderr, "CUDA error: %s\n", cudaGetErrorString(err));
#     }
# }

# Example main.cpp (host code):
#
# #include <iostream>
# #include <vector>
# #include <chrono>
#
# extern "C" void launchVectorAdd(const float*, const float*, float*, int);
#
# int main() {
#     const int N = 1 << 20;  // 1M elements
#     std::vector<float> h_A(N, 1.0f);
#     std::vector<float> h_B(N, 2.0f);
#     std::vector<float> h_C(N);
#
#     auto start = std::chrono::high_resolution_clock::now();
#     launchVectorAdd(h_A.data(), h_B.data(), h_C.data(), N);
#     auto end = std::chrono::high_resolution_clock::now();
#
#     std::chrono::duration<double, std::milli> elapsed = end - start;
#     std::cout << "Time: " << elapsed.count() << " ms\n";
#
#     // Verify result
#     bool success = true;
#     for (int i = 0; i < N; i++) {
#         if (std::abs(h_C[i] - 3.0f) > 1e-5) {
#             success = false;
#             break;
#         }
#     }
#     std::cout << (success ? "PASS" : "FAIL") << "\n";
#
#     return success ? 0 : 1;
# }

# Performance optimization for Jetson Orin:
#
# 1. Use shared memory for frequently accessed data
# __shared__ float sdata[256];
#
# 2. Coalesce global memory accesses (aligned, contiguous)
# int idx = blockIdx.x * blockDim.x + threadIdx.x;
#
# 3. Optimize occupancy (balance registers vs shared memory)
# Use --ptxas-options=-v to see register usage
#
# 4. Use streams for concurrent operations
# cudaStream_t stream1, stream2;
# cudaStreamCreate(&stream1);
# kernel<<<grid, block, 0, stream1>>>(...)
#
# 5. Use unified memory for easier development (may be slower)
# cudaMallocManaged(&ptr, size);

# Runtime testing on target:
#
# # Check CUDA installation
# nvidia-smi                              # Show GPU status
# nvcc --version                          # CUDA compiler version
# ls -l /usr/local/cuda                   # CUDA installation
#
# # Run sample
# cuda-sample                             # Run with defaults
# cuda-sample --size 1048576              # Specific problem size
#
# # Monitor GPU usage
# tegrastats                              # Jetson-specific monitoring
# watch -n 1 nvidia-smi                   # GPU utilization
#
# # Profile performance
# nsys profile cuda-sample                # Nsight Systems profiling
# ncu cuda-sample                         # Nsight Compute analysis

# Common CUDA optimization flags:
# --use_fast_math: Use fast but less precise math functions
# --maxrregcount=32: Limit register usage per thread
# -Xptxas -dlcm=ca: Cache at all levels
# -lineinfo: Include source line info (for profiling)
