cmake_minimum_required(VERSION 3.16)

project(decrypt)

# 先定义目标
add_library(decrypt SHARED
    decrypt.c
)

# 再设置 include 目录
target_include_directories(decrypt PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/../lua
    ${CMAKE_CURRENT_SOURCE_DIR}/../luajava
)

# 链接 log（Android）
if(ANDROID)
    find_library(LOG_LIB log)
    target_link_libraries(decrypt ${LOG_LIB})
endif()

# 编译选项
target_compile_options(decrypt PRIVATE
    -O2
    -Wall
    -fvisibility=hidden
    -DLUA_COMPAT_5_2
    -DLUA_COMPAT_5_1
)

# 导出 luaopen_decrypt 符号
set_target_properties(decrypt PROPERTIES
    C_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN YES
)

# Android 链接器导出符号
if(ANDROID)
    target_link_options(decrypt PRIVATE
        "-Wl,--export-dynamic-symbol=luaopen_decrypt"
    )
endif()
