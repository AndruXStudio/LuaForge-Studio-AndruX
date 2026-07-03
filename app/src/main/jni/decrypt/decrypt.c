/*
 * libdecrypt.so - Lua 加密脚本加载器
 * 与 ldump.c / lundump.c 使用相同的保护配置
 * 提供: decrypt.loadfile(path) -> string, is_bytecode
 *       decrypt.dofile(path)   -> 加载并执行
 *       decrypt.load(path)     -> 加载为函数
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>

#include "lua.h"
#include "lauxlib.h"
#include "lualib.h"

/* ============ 与 ldump.c / lundump.c 完全相同的保护配置 ============ */
#ifndef LUA_PROTECT_KEY
#define LUA_PROTECT_KEY 0x89ABCDEF
#endif

#ifndef LUA_PROTECT_FLAGS
#define LUA_PROTECT_FLAGS 0x03
#endif

/* 加密文件魔数 */
#define DECRYPT_MAGIC       "\x1BLuaE"
#define DECRYPT_MAGIC_LEN   5

/* 加密文件头（小端序） */
typedef struct {
    char magic[DECRYPT_MAGIC_LEN];      /* 魔数 */
    uint8_t version;                     /* 版本号 */
    uint8_t flags;                       /* 加密标志 */
    uint32_t key_seed;                   /* 密钥种子 */
    uint32_t orig_size;                  /* 原始数据大小 */
    uint32_t padded_size;                /* 填充后大小 */
    uint8_t reserved[8];                 /* 保留 */
} __attribute__((packed)) EncryptHeader;

/* ============ 密钥派生（与 lundump.c 兼容） ============ */

static uint32_t derive_key(uint32_t seed) {
    uint32_t key = seed ^ LUA_PROTECT_KEY;
    key = (key * 0x9E3779B9) ^ (key >> 16);
    key ^= 0xA5A5A5A5;
    return key;
}

/* 解密单个字节（与 lundump.c 的 unprotect_byte 一致） */
static inline uint8_t unprotect_byte(uint8_t b, int idx, uint32_t key) {
    uint8_t k = (key >> (8 * (idx % 4))) & 0xFF;
    b ^= k;
    b = (b >> 3) | (b << 5);  /* 循环右移3位 */
    b ^= (idx & 0xFF);
    return b;
}

/* 加密单个字节（与 ldump.c 的 protect_byte 一致） */
static inline uint8_t protect_byte(uint8_t b, int idx, uint32_t key) {
    uint8_t k = (key >> (8 * (idx % 4))) & 0xFF;
    b ^= (idx & 0xFF);
    b = (b << 3) | (b >> 5);  /* 循环左移3位 */
    b ^= k;
    return b;
}

/* ============ 文件读取 ============ */

static uint8_t *read_file(const char *path, size_t *out_size) {
    FILE *fp = fopen(path, "rb");
    if (!fp) return NULL;

    fseek(fp, 0, SEEK_END);
    long size = ftell(fp);
    if (size < 0) {
        fclose(fp);
        return NULL;
    }
    fseek(fp, 0, SEEK_SET);

    uint8_t *data = (uint8_t *)malloc((size_t)size);
    if (!data) {
        fclose(fp);
        return NULL;
    }

    if (fread(data, 1, (size_t)size, fp) != (size_t)size) {
        free(data);
        fclose(fp);
        return NULL;
    }

    fclose(fp);
    *out_size = (size_t)size;
    return data;
}

/* ============ 解密核心 ============ */

static uint8_t *decrypt_buffer(const uint8_t *input, size_t input_size,
                               size_t *out_size, int *is_bytecode) {
    *out_size = 0;
    *is_bytecode = 0;

    /* 检查是否是加密文件 */
    if (input_size < sizeof(EncryptHeader) ||
        memcmp(input, DECRYPT_MAGIC, DECRYPT_MAGIC_LEN) != 0) {
        /* 不是加密文件，直接返回原始内容 */
        uint8_t *plain = (uint8_t *)malloc(input_size + 1);
        if (!plain) return NULL;
        memcpy(plain, input, input_size);
        plain[input_size] = '\0';
        *out_size = input_size;

        /* 检测是否是 Lua 字节码 */
        if (input_size >= 4 && plain[0] == '\x1b' &&
            plain[1] == 'L' && plain[2] == 'u' && plain[3] == 'a') {
            *is_bytecode = 1;
        }
        return plain;
    }

    /* 是加密文件 */
    const EncryptHeader *hdr = (const EncryptHeader *)input;
    uint32_t key = derive_key(hdr->key_seed);
    size_t data_offset = sizeof(EncryptHeader);
    size_t data_size = input_size - data_offset;

    if (data_size < hdr->orig_size) {
        return NULL; /* 文件损坏 */
    }

    uint8_t *plain = (uint8_t *)malloc(hdr->orig_size + 1);
    if (!plain) return NULL;

    for (size_t i = 0; i < hdr->orig_size; i++) {
        plain[i] = unprotect_byte(input[data_offset + i], (int)i, key);
    }
    plain[hdr->orig_size] = '\0';
    *out_size = hdr->orig_size;

    /* 检测是否是 Lua 字节码 */
    if (hdr->orig_size >= 4 && plain[0] == '\x1b' &&
        plain[1] == 'L' && plain[2] == 'u' && plain[3] == 'a') {
        *is_bytecode = 1;
    }

    return plain;
}

/* ============ Lua 接口函数 ============ */

/*
 * decrypt.loadfile(path)
 * 读取文件，自动解密，返回 (content, is_bytecode)
 */
static int decrypt_loadfile(lua_State *L) {
    const char *path = luaL_checkstring(L, 1);

    size_t file_size = 0;
    uint8_t *file_data = read_file(path, &file_size);
    if (!file_data) {
        return luaL_error(L, "cannot open file: %s", path);
    }

    size_t plain_size = 0;
    int is_bytecode = 0;
    uint8_t *plain = decrypt_buffer(file_data, file_size, &plain_size, &is_bytecode);
    free(file_data);

    if (!plain) {
        return luaL_error(L, "decrypt failed: %s", path);
    }

    lua_pushlstring(L, (const char *)plain, plain_size);
    lua_pushboolean(L, is_bytecode);
    free(plain);
    return 2;
}

/*
 * decrypt.dofile(path)
 * 读取文件，解密，编译并执行
 */
static int decrypt_dofile(lua_State *L) {
    const char *path = luaL_checkstring(L, 1);

    /* 获取文件内容 */
    lua_pushcfunction(L, decrypt_loadfile);
    lua_pushstring(L, path);
    if (lua_pcall(L, 1, 2, 0) != LUA_OK) {
        return lua_error(L);
    }

    size_t len = 0;
    const char *data = lua_tolstring(L, -2, &len);
    int is_bytecode = lua_toboolean(L, -1);

    /* 编译 */
    const char *chunkname = lua_pushfstring(L, "@%s", path);
    int status;
    if (is_bytecode) {
        status = luaL_loadbuffer(L, data, len, chunkname);
    } else {
        status = luaL_loadstring(L, data);
    }
    lua_remove(L, -2); /* 移除 chunkname */
    lua_remove(L, -2); /* 移除 is_bytecode */
    lua_remove(L, -2); /* 移除 data */

    if (status != LUA_OK) {
        return lua_error(L);
    }

    /* 执行 */
    lua_call(L, 0, LUA_MULTRET);
    return lua_gettop(L);
}

/*
 * decrypt.load(path)
 * 读取文件，解密，编译为函数但不执行
 */
static int decrypt_load(lua_State *L) {
    const char *path = luaL_checkstring(L, 1);

    lua_pushcfunction(L, decrypt_loadfile);
    lua_pushstring(L, path);
    if (lua_pcall(L, 1, 2, 0) != LUA_OK) {
        return lua_error(L);
    }

    size_t len = 0;
    const char *data = lua_tolstring(L, -2, &len);
    int is_bytecode = lua_toboolean(L, -1);

    const char *chunkname = lua_pushfstring(L, "@%s", path);
    int status;
    if (is_bytecode) {
        status = luaL_loadbuffer(L, data, len, chunkname);
    } else {
        status = luaL_loadstring(L, data);
    }
    lua_remove(L, -2); /* chunkname */
    lua_remove(L, -2); /* is_bytecode */
    lua_remove(L, -2); /* data */

    if (status != LUA_OK) {
        return lua_error(L);
    }

    return 1; /* 返回函数 */
}

/*
 * decrypt.encrypt(data, seed)
 * 加密字符串，返回加密后的二进制数据
 */
static int decrypt_encrypt(lua_State *L) {
    size_t len = 0;
    const char *data = luaL_checklstring(L, 1, &len);
    uint32_t seed = (uint32_t)luaL_optinteger(L, 2, 0x12345678);

    uint32_t key = derive_key(seed);

    EncryptHeader hdr;
    memset(&hdr, 0, sizeof(hdr));
    memcpy(hdr.magic, DECRYPT_MAGIC, DECRYPT_MAGIC_LEN);
    hdr.version = 1;
    hdr.flags = LUA_PROTECT_FLAGS;
    hdr.key_seed = seed;
    hdr.orig_size = (uint32_t)len;
    hdr.padded_size = (uint32_t)len;

    size_t total = sizeof(hdr) + len;
    uint8_t *output = (uint8_t *)malloc(total);
    if (!output) {
        return luaL_error(L, "out of memory");
    }

    memcpy(output, &hdr, sizeof(hdr));
    for (size_t i = 0; i < len; i++) {
        output[sizeof(hdr) + i] = protect_byte((uint8_t)data[i], (int)i, key);
    }

    lua_pushlstring(L, (const char *)output, total);
    free(output);
    return 1;
}

/*
 * decrypt.decrypt(data)
 * 解密字符串，返回 (plain, is_bytecode)
 */
static int decrypt_decrypt(lua_State *L) {
    size_t len = 0;
    const char *data = luaL_checklstring(L, 1, &len);

    size_t out_size = 0;
    int is_bytecode = 0;
    uint8_t *plain = decrypt_buffer((const uint8_t *)data, len, &out_size, &is_bytecode);

    if (!plain) {
        return luaL_error(L, "decrypt failed");
    }

    lua_pushlstring(L, (const char *)plain, out_size);
    lua_pushboolean(L, is_bytecode);
    free(plain);
    return 2;
}

/* ============ 模块注册 ============ */

static const luaL_Reg decrypt_funcs[] = {
    {"loadfile", decrypt_loadfile},
    {"dofile",   decrypt_dofile},
    {"load",     decrypt_load},
    {"encrypt",  decrypt_encrypt},
    {"decrypt",  decrypt_decrypt},
    {NULL, NULL}
};

__attribute__((visibility("default"))) int luaopen_decrypt(lua_State *L) {
    luaL_newlib(L, decrypt_funcs);
    return 1;
}
