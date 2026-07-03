cmake_minimum_required(VERSION 3.16)

project(decrypt)

add_library(decrypt SHARED
    decrypt.c
)

target_include_directories(decrypt PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/../lua
    ${CMAKE_CURRENT_SOURCE_DIR}/../luajava
)

target_link_libraries(decrypt
    lua
)

if(ANDROID)
    find_library(LOG_LIB log)
    target_link_libraries(decrypt ${LOG_LIB})
endif()

target_compile_options(decrypt PRIVATE
    -O2
    -Wall
    -fvisibility=hidden
    -DLUA_COMPAT_5_2
    -DLUA_COMPAT_5_1
)

set_target_properties(decrypt PROPERTIES
    C_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN YES
)

if(ANDROID)
    target_link_options(decrypt PRIVATE
        "-Wl,--export-dynamic-symbol=luaopen_decrypt"
    )
endif()
