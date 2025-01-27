#include <linux/fs.h>
#include <linux/string.h>
#include <linux/dcache.h>

// get field offset in kv string, ret 0 on success
int get_field_range(const char* key, int* begin, int* end, char* buf) {
    int offset = 0;
    char tmp[64];
    int iter = 0;
    char is_key = 1;
    char is_required_field = 0;
    while (*buf) {
        if (*buf == ';') {
            tmp[iter] = '\0';
            iter = 0;
            if (is_required_field) {
                *end = offset;
                return 0;
            }
            if (strcmp(tmp, key) == 0) {
                is_required_field = 1;
                *begin = offset + 1;
            }
            is_key = !is_key;
        } else if (is_key) {
            tmp[iter++] = *buf;
        }
        offset++;
        buf++;
    }
    return 1;
}

char* vtfs_get_full_path(struct dentry *dentry, char *buf, size_t size) {
    char *path = buf + size - 1;
    struct dentry *cur = dentry;
    *path = '\0';
    *(--path) = '\0';

    while (cur && !IS_ROOT(cur)) {
        size_t len = strlen(cur->d_name.name);
        if (path - len - 1 < buf) {
            printk(KERN_ERR "Buffer too small for path\n");
            return 0;
        }

        path -= len;
        memcpy(path, cur->d_name.name, len);

        *(--path) = '/';
        cur = cur->d_parent;
    }
    
    if (cur == dentry->d_sb->s_root) {
        *(--path) = '/'; // for root
    }
    return path;
}

inline int convert_type(int type) {
    if (type == '1') type = DT_REG;
    else if (type == '2') type = DT_DIR;
    else if (type == '3') type = DT_LNK;
    else type = DT_REG;
    return type;
}  

inline int convert_type2(int type) {
    if (type == '1') type = S_IFREG;
    else if (type == '2') type = S_IFDIR;
    else if (type == '3') type = S_IFLNK;
    else type = DT_REG;
    return type;
}  
