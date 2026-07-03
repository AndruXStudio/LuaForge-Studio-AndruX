cmake_minimum_required(VERSION 3.16)

# 引用与 luaparser 相同的头文件路径
target_include_directories(decrypt PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/../lua
    ${CMAKE_CURRENT_SOURCE_DIR}/../luajava
)

add_library(decrypt SHARED
    decrypt.c
)

# 链接 log（Android）
if(ANDROID)
    find_library(LOG_LIB log)
    target_link_libraries(decrypt ${LOG_LIB})
endif()

# 编译选项 - 与主项目保持一致
target_compile_options(decrypt PRIVATE
    -O2
    -Wall
    -fvisibility=hidden
    -DLUA_COMPAT_5_2
    -DLUA_COMPAT_5_1
)

# 关键：导出 luaopen_decrypt 符号
set_target_properties(decrypt PROPERTIES
    C_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN YES
)

# 确保 luaopen_decrypt 可见（Android 链接器需要）
if(ANDROID)
    target_link_options(decrypt PRIVATE
        "-Wl,--export-dynamic-symbol=luaopen_decrypt"
    )
endif()