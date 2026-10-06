cmake_minimum_required(VERSION 3.22.1)

project("duanju_jni")

# 站源核心由 Go 构建（scripts/build_player.py -> jniLibs/<abi>/libduanju_core.so）。
set(duanju_core_dir ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
set(duanju_core_lib ${duanju_core_dir}/libduanju_core.so)

if(NOT EXISTS ${duanju_core_lib})
    message(FATAL_ERROR "缺少站源核心库 ${duanju_core_lib}，请先运行 python scripts/build_player.py")
endif()

add_library(duanju_core SHARED IMPORTED)
set_target_properties(duanju_core PROPERTIES IMPORTED_LOCATION ${duanju_core_lib})

add_library(duanju_jni SHARED duanju_jni.c)

find_library(log-lib log)

target_link_libraries(duanju_jni duanju_core ${log-lib})
