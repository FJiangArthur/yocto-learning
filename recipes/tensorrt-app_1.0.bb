# tensorrt-app_1.0.bb - TensorRT inference application recipe
#
# This recipe demonstrates:
# - TensorRT SDK integration for optimized inference
# - ONNX model conversion and deployment
# - INT8/FP16 quantization for embedded GPUs
# - Batch inference and dynamic shapes
# - Integration with camera input (V4L2/GStreamer)
#
# Learning objectives:
# 1. Understand TensorRT optimization pipeline
# 2. Learn model quantization for embedded deployment
# 3. Practice inference engine building and caching
# 4. Handle multi-stream inference workloads

SUMMARY = "TensorRT inference application for Jetson"
DESCRIPTION = "Production-ready TensorRT application demonstrating AI inference \
optimization on NVIDIA Jetson platforms. Includes ONNX model parsing, engine \
building, INT8 calibration, and real-time inference pipeline."

HOMEPAGE = "https://github.com/example/tensorrt-inference"
SECTION = "ai"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=34400b68072d710fecd0a2940a0d1658"

SRC_URI = " \
    git://github.com/example/tensorrt-app.git;protocol=https;branch=main \
    file://tensorrt-app.service \
    file://model_config.yaml \
"

SRCREV = "abcdef1234567890abcdef1234567890abcdef12"

S = "${WORKDIR}/git"

# Build dependencies
# tensorrt: TensorRT SDK (includes nvinfer, nvonnxparser)
# cuda-toolkit: CUDA development tools
# opencv: For image preprocessing
DEPENDS = " \
    tensorrt \
    cuda-toolkit \
    cudnn \
    opencv \
    gstreamer1.0-plugins-nvvideoconvert \
"

# Runtime dependencies
RDEPENDS:${PN} = " \
    tensorrt-libs \
    cuda-libraries \
    libcudart \
    libcublas \
    libcudnn \
    opencv \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-nvarguscamerasrc \
    python3-pycuda \
"

# Jetson-specific (TensorRT requires CUDA)
COMPATIBLE_MACHINE = "(tegra234|jetson-orin.*|jetson-xavier.*)"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit cmake systemd cuda

# TensorRT specific variables
TENSORRT_VERSION = "8.6"

# CUDA architecture for TensorRT optimization
CUDA_ARCH = "sm_87"  # Orin

# CMake configuration for TensorRT
EXTRA_OECMAKE = " \
    -DTENSORRT_ROOT=${STAGING_DIR_HOST}/usr \
    -DCUDA_TOOLKIT_ROOT_DIR=${STAGING_DIR_HOST}/usr/local/cuda \
    -DCUDNN_ROOT=${STAGING_DIR_HOST}/usr \
    -DOPENCV_DIR=${STAGING_DIR_HOST}/usr \
    -DBUILD_SAMPLES=ON \
    -DBUILD_PLUGINS=ON \
    -DENABLE_INT8=ON \
    -DENABLE_FP16=ON \
    -DCUDA_ARCH=${CUDA_ARCH} \
"

# Compiler flags for TensorRT applications
CXXFLAGS:append = " \
    -std=c++14 \
    -O3 \
    -I${STAGING_INCDIR}/tensorrt \
    -DTRT_VERSION=${TENSORRT_VERSION} \
"

LDFLAGS:append = " \
    -L${STAGING_LIBDIR}/tensorrt \
    -lnvinfer \
    -lnvonnxparser \
    -lnvinfer_plugin \
    -lnvparsers \
"

do_install:append() {
    # Install TensorRT application
    install -d ${D}${bindir}
    install -m 0755 ${B}/tensorrt-app ${D}${bindir}/

    # Install model configuration
    install -d ${D}${sysconfdir}/tensorrt-app
    install -m 0644 ${WORKDIR}/model_config.yaml \
        ${D}${sysconfdir}/tensorrt-app/

    # Install systemd service for inference server
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/tensorrt-app.service \
        ${D}${systemd_system_unitdir}/

    # Create model cache directory
    install -d ${D}${localstatedir}/lib/tensorrt-app/engines
    install -d ${D}${localstatedir}/lib/tensorrt-app/models
    install -d ${D}${localstatedir}/lib/tensorrt-app/calibration

    # Install example models (if included)
    if [ -d ${S}/models ]; then
        cp -r ${S}/models/*.onnx ${D}${localstatedir}/lib/tensorrt-app/models/
    fi

    # Install calibration data (for INT8)
    if [ -d ${S}/calibration ]; then
        cp -r ${S}/calibration/* ${D}${localstatedir}/lib/tensorrt-app/calibration/
    fi
}

SYSTEMD_SERVICE:${PN} = "tensorrt-app.service"
SYSTEMD_AUTO_ENABLE = "disable"

FILES:${PN} = " \
    ${bindir}/tensorrt-app \
    ${sysconfdir}/tensorrt-app \
    ${systemd_system_unitdir}/tensorrt-app.service \
    ${localstatedir}/lib/tensorrt-app \
"

# Example C++ code for TensorRT inference (main.cpp):
#
# #include <NvInfer.h>
# #include <NvOnnxParser.h>
# #include <cuda_runtime_api.h>
# #include <fstream>
# #include <vector>
#
# using namespace nvinfer1;
#
# class Logger : public ILogger {
#     void log(Severity severity, const char* msg) noexcept override {
#         if (severity <= Severity::kWARNING)
#             std::cout << msg << std::endl;
#     }
# } gLogger;
#
# ICudaEngine* buildEngineFromONNX(const std::string& onnxPath) {
#     // Create builder
#     IBuilder* builder = createInferBuilder(gLogger);
#     INetworkDefinition* network = builder->createNetworkV2(
#         1U << static_cast<uint32_t>(NetworkDefinitionCreationFlag::kEXPLICIT_BATCH));
#
#     // Parse ONNX
#     nvonnxparser::IParser* parser = nvonnxparser::createParser(*network, gLogger);
#     parser->parseFromFile(onnxPath.c_str(),
#         static_cast<int>(ILogger::Severity::kWARNING));
#
#     // Build config
#     IBuilderConfig* config = builder->createBuilderConfig();
#     config->setMaxWorkspaceSize(1ULL << 30);  // 1GB
#
#     // Enable FP16 for Jetson
#     if (builder->platformHasFastFp16()) {
#         config->setFlag(BuilderFlag::kFP16);
#     }
#
#     // Enable INT8 (requires calibration)
#     // if (builder->platformHasFastInt8()) {
#     //     config->setFlag(BuilderFlag::kINT8);
#     //     config->setInt8Calibrator(calibrator);
#     // }
#
#     // Build engine
#     ICudaEngine* engine = builder->buildEngineWithConfig(*network, *config);
#
#     // Cleanup
#     delete parser;
#     delete network;
#     delete config;
#     delete builder;
#
#     return engine;
# }
#
# void runInference(ICudaEngine* engine, const std::vector<float>& input) {
#     IExecutionContext* context = engine->createExecutionContext();
#
#     // Allocate buffers
#     void* buffers[2];  // input and output
#     cudaMalloc(&buffers[0], input.size() * sizeof(float));
#     cudaMalloc(&buffers[1], 1000 * sizeof(float));  // assuming 1000 classes
#
#     // Copy input to device
#     cudaMemcpy(buffers[0], input.data(),
#                input.size() * sizeof(float), cudaMemcpyHostToDevice);
#
#     // Run inference
#     context->executeV2(buffers);
#
#     // Copy output to host
#     std::vector<float> output(1000);
#     cudaMemcpy(output.data(), buffers[1],
#                output.size() * sizeof(float), cudaMemcpyDeviceToHost);
#
#     // Cleanup
#     cudaFree(buffers[0]);
#     cudaFree(buffers[1]);
#     delete context;
# }

# Example model configuration (model_config.yaml):
#
# models:
#   - name: resnet50
#     format: onnx
#     path: /var/lib/tensorrt-app/models/resnet50.onnx
#     precision: fp16
#     max_batch_size: 4
#     workspace_size: 1073741824  # 1GB
#     cache_engine: true
#     engine_path: /var/lib/tensorrt-app/engines/resnet50_fp16.trt
#
#   - name: yolov5
#     format: onnx
#     path: /var/lib/tensorrt-app/models/yolov5s.onnx
#     precision: int8
#     calibration_data: /var/lib/tensorrt-app/calibration/coco_val
#     max_batch_size: 1
#     dynamic_shapes:
#       input:
#         min: [1, 3, 384, 384]
#         opt: [1, 3, 640, 640]
#         max: [1, 3, 1280, 1280]
#
# inference:
#   num_streams: 2
#   dla_core: -1  # -1 for GPU, 0 or 1 for DLA
#   device_id: 0

# Example systemd service (tensorrt-app.service):
#
# [Unit]
# Description=TensorRT Inference Server
# After=nvidia-l4t-core.service
#
# [Service]
# Type=simple
# User=tensorrt
# Group=tensorrt
# ExecStart=/usr/bin/tensorrt-app --config /etc/tensorrt-app/model_config.yaml
# Restart=on-failure
# RestartSec=5
#
# # GPU access required
# DeviceAllow=/dev/nvidia0 rw
# DeviceAllow=/dev/nvidiactl rw
# DeviceAllow=/dev/nvidia-modeset rw
#
# [Install]
# WantedBy=multi-user.target

# TensorRT optimization workflow:
#
# 1. Convert PyTorch/TensorFlow model to ONNX
#    python3 -m torch.onnx.export model.pth model.onnx
#
# 2. Build TensorRT engine with optimizations
#    trtexec --onnx=model.onnx --saveEngine=model.trt --fp16
#
# 3. INT8 calibration (optional)
#    trtexec --onnx=model.onnx --int8 --calib=calibration_data
#
# 4. Benchmark performance
#    trtexec --loadEngine=model.trt --iterations=1000
#
# 5. Profile with Nsight
#    nsys profile tensorrt-app

# Performance tips for Jetson:
# - Use FP16 precision (2-3x speedup vs FP32)
# - Use INT8 with calibration (3-5x speedup, minimal accuracy loss)
# - Enable DLA cores for power efficiency (Orin has 2 DLA cores)
# - Use CUDA streams for concurrent execution
# - Batch multiple inferences when possible
# - Cache TensorRT engines (building is slow, inference is fast)
