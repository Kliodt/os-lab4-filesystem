#include <linux/init.h>
#include <linux/module.h>
#include <linux/printk.h>
#include <linux/fs.h>
#include <linux/uaccess.h>

#include "http.c"
#include "utils.c"

#define MODULE_NAME "vtfs"

MODULE_LICENSE("GPL");
MODULE_AUTHOR("secs-dev");
MODULE_DESCRIPTION("A simple FS kernel module");

#define LOG(fmt, ...) pr_info("[" MODULE_NAME "]: " fmt, ##__VA_ARGS__)


// --------------------- declarations ---------------------

struct dentry* vtfs_mount(struct file_system_type* fs_type, int flags, const char* token, void* data);
int vtfs_fill_super(struct super_block *sb, void *data, int silent);
struct inode* vtfs_get_inode(struct super_block* sb, const struct inode* dir, umode_t mode, int i_ino);
void vtfs_kill_sb(struct super_block* sb);
struct dentry* vtfs_lookup(struct inode* parent_inode, struct dentry* child_dentry, unsigned int flag);
int vtfs_iterate(struct file* filp, struct dir_context* ctx);
int vtfs_create(struct user_namespace *user_namespace, struct inode *parent_inode, struct dentry *child_dentry, umode_t mode, bool b);
int vtfs_unlink(struct inode *parent_inode, struct dentry *child_dentry);
int vtfs_mkdir(struct user_namespace * ns, struct inode *parent_dir, struct dentry *dentry, umode_t mode);
int vtfs_rmdir(struct inode *parent_dir, struct dentry *dentry);
ssize_t vtfs_read(struct file *filp, char *buffer, size_t len, loff_t *offset);
ssize_t vtfs_write(struct file *filp, const char *buffer, size_t len, loff_t *offset);

// --------------------- structures ---------------------

// структура с описанием файловой системы.
struct file_system_type vtfs_fs_type = {
  .name = "vtfs",
  .mount = vtfs_mount,
  .kill_sb = vtfs_kill_sb,
};

// структура с описанием операций inode
struct inode_operations vtfs_inode_ops = {
  .lookup = vtfs_lookup,
  .create = vtfs_create,
  .unlink = vtfs_unlink,
  .mkdir = vtfs_mkdir,
  .rmdir = vtfs_rmdir,
};

// структура с описанием операций файлов
struct file_operations vtfs_dir_ops = {
  .iterate = vtfs_iterate,
  .read = vtfs_read,
  .write = vtfs_write,
};


// --------------------- functions ---------------------

// mount filesystem
struct dentry* vtfs_mount(struct file_system_type* fs_type, int flags, const char* token, void* data) {
  struct dentry* ret = mount_nodev(fs_type, flags, data, vtfs_fill_super);
  if (ret == NULL) {
    printk(KERN_ERR "Can't mount file system");
  } else {
    printk(KERN_INFO "Mounted successfuly");
  }
  return ret;
}

// fill super_block
int vtfs_fill_super(struct super_block *sb, void *data, int silent) {
  struct inode* inode = vtfs_get_inode(sb, NULL, S_IFDIR | S_IRWXUGO, 100); // create inode for root dir

  sb->s_root = d_make_root(inode);

  if (sb->s_root == NULL) {
    printk(KERN_INFO "fill super return OUT OF MEMORY\n");
    return -ENOMEM;
  }

  printk(KERN_INFO "fill super return 0\n");
  return 0;
}

// создать структуру inode в директории dir, с режимом mode и номером i_ino
struct inode* vtfs_get_inode(struct super_block* sb, const struct inode* dir, umode_t mode, int i_ino) {
  struct inode *inode = new_inode(sb);
  if (inode != NULL) {
    inode_init_owner(sb->s_user_ns, inode, dir, mode);
    inode->i_ino = i_ino;
    inode->i_op = &vtfs_inode_ops;
    inode->i_mode = mode;
    inode->i_fop = &vtfs_dir_ops;
    // if(S_ISDIR(inode->i_mode)) {
    // } else if(S_ISREG(inode->i_mode)) {

    // } else if(S_ISLNK(inode->i_mode)) {

    // }
  }
  return inode;
}

// вызывается при отмонтировании файловой системы
void vtfs_kill_sb(struct super_block* sb) {
  printk(KERN_INFO "vtfs super block is destroyed. Unmount successfully.\n");
}

// parent_node - родительская нода
// child_dentry - объект, к которому мы пытаемся получить доступ
// flag - не исп.
// Search for a dentry in dir. Fills dentry with NULL if not found in dir, or with the corresponding inode if found
// Returns NULL on success, indicating the dentry was successfully filled or confirmed absent.
struct dentry* vtfs_lookup(struct inode* parent_inode, struct dentry* child_dentry, unsigned int flag) {
  // ino_t root = parent_inode->i_ino;
  // const char *name = child_dentry->d_name.name;
  char buf[1024];
  char* path = vtfs_get_full_path(child_dentry, buf, 1024);

  // printk(KERN_INFO "Full path to dentry: %s\n", path);

  int res = vtfs_http_call("idk", "dir/entry_info", buf, 1024, 1, "path", path);

  // printk(KERN_INFO "Request result: %s\n", buf);

  if (res != 0) {
    return NULL;
  }

  if (buf[2] != '1') { // server returned err
    return NULL;
  }

  // get entry info
  int begin, end;
  if (get_field_range("3", &begin, &end, buf) != 0) {
    return NULL;
  }

  int type = buf[begin];
  type = convert_type2(type);

  struct inode *inode = vtfs_get_inode(parent_inode->i_sb, NULL, type | S_IRWXUGO, 100);
  d_add(child_dentry, inode);

  return NULL;
}


int vtfs_iterate(struct file *filp, struct dir_context *ctx) {

  // printk(KERN_INFO "Context pos = %lld\n", ctx->pos);
  if (ctx->pos > 0) {
    return 0;
  }

  char* buf = kmalloc(4096, GFP_KERNEL); // and use it for everything
  char* path = vtfs_get_full_path(filp->f_path.dentry, buf, 4096);

  // printk(KERN_INFO "Full path: %s\n", path);

  int res = vtfs_http_call("idk", "dir/get", buf, 4096, 1, "path", path);

  // printk(KERN_INFO "Request result: %s\n", buf);

  if (res != 0) {
    kfree(buf);
    return 1;
  }

  // get directory list
  int begin, end;
  if (get_field_range("3", &begin, &end, buf) != 0) {
    kfree(buf);
    return 1;
  }

  while (1) {
    // get type and name
    char name[64];
    unsigned char type;
    int name_iter = 0;
    while (buf[begin] != ',' && begin < end) {
      type = buf[begin];
      begin++;
    }
    begin++;

    type = convert_type(type);

    while (buf[begin] != ',' && begin < end) {
      name[name_iter++] = buf[begin];
      begin++;
    }
    begin++;
    name[name_iter] = '\0';
    if (name_iter == 0) break;
  
    // 100 - dont care about inodes
    if (!dir_emit(ctx, name, strlen(name), 100, type)) {
      kfree(buf);
      return -EINVAL;
    }
    ctx->pos++;

    if (begin == end) break; // end of list
  }

  kfree(buf);
  return 1;
}

// create file, return 0 on success
int vtfs_create(struct user_namespace *user_namespace, struct inode *parent_inode, struct dentry *child_dentry, umode_t mode, bool b) {
  const char *name = child_dentry->d_name.name;

  char buf[1024];
  char* path = vtfs_get_full_path(child_dentry->d_parent, buf, 1024);

  // printk(KERN_INFO "vtfs CREATE: %s\n", path);

  int res = vtfs_http_call("idk", "file/create", buf, 1024, 2, "path", path, "name", name);

  if (res != 0 || buf[2] != '1') {
    return 1;
  }

  struct inode *inode = vtfs_get_inode(parent_inode->i_sb, NULL, S_IFREG | S_IRWXUGO, 100);
  // inode->i_fop = &vtfs_dir_ops;
  
  d_add(child_dentry, inode);

  return 0;
}

// decrement ref count of a file, delete file if ref count = 0; return 0 on success
int vtfs_unlink(struct inode *parent_inode, struct dentry *child_dentry) {
  
  char buf[1024];
  char* path = vtfs_get_full_path(child_dentry, buf, 1024);

  int res = vtfs_http_call("idk", "file/unlink", buf, 1024, 1, "path", path);

  if (res != 0 || buf[2] != '1') {
    return 1;
  }
  return 0;
}

// create dir, return 0 on success
int vtfs_mkdir(struct user_namespace * ns,struct inode *parent_dir, struct dentry *dentry, umode_t mode) {
  const char *name = dentry->d_name.name;

  char buf[1024];
  char* path = vtfs_get_full_path(dentry->d_parent, buf, 1024);

  int res = vtfs_http_call("idk", "dir/create", buf, 1024, 2, "path", path, "name", name);

  if (res != 0 || buf[2] != '1') {
    return 1;
  }

  struct inode *inode = vtfs_get_inode(parent_dir->i_sb, NULL, S_IFDIR | S_IRWXUGO, 100);
  d_add(dentry, inode);

  return 0;
}

// delete dir, return 0 on success
int vtfs_rmdir(struct inode *parent_dir, struct dentry *dentry) {
  // const char *name = dentry->d_name.name;

  char buf[1024];
  char* path = vtfs_get_full_path(dentry, buf, 1024);

  int res = vtfs_http_call("idk", "dir/remove", buf, 1024, 1, "path", path);

  if (res != 0) {
    return 1;
  }

  if (buf[2] != '1') {
    return -ENOTEMPTY;
  }

  struct inode *inode = vtfs_get_inode(parent_dir->i_sb, NULL, S_IFDIR | S_IRWXUGO, 100);
  d_add(dentry, inode);

  return 0;
}

ssize_t vtfs_read(struct file *filp, char *buffer, size_t len, loff_t *offset) {
  char path_buf[1024];
  char* path = vtfs_get_full_path(filp->f_path.dentry, path_buf, 1024);
  
  char offsetbuf[20] = {0};
  sprintf(offsetbuf,"%lld",*offset);

  char* text = kmalloc(len + 1024, GFP_KERNEL);
  int res = vtfs_http_call("idk", "file/read", text, 1024, 2, "path", path, "offset", offsetbuf);

  // printk(KERN_INFO "Fiel path (write): %s, offset: %lld, len %ld; http_resulr: %d; result: %s\n", path, *offset, len, res, text);

  int begin, end;
  if(get_field_range("2", &begin, &end, text) != 0) {
    kfree(text);
    return 0;
  }

  copy_to_user(buffer, text + begin, end - begin);
  kfree(text);
  *offset += end - begin;  
  return end - begin;
}

ssize_t vtfs_write(struct file *filp, const char *buffer, size_t len, loff_t *offset) {
  // todo: offset
  char offsetbuf[20] = {0};
  sprintf(offsetbuf,"%lld",*offset);

  char* text = kmalloc(len + 10, GFP_KERNEL);

  int cop = copy_from_user(text, buffer, len-2); // other fix for newline
  text[len-1] = '\0';
  // printk(KERN_INFO "buffer %s\n", text);

  char buf[1024];
  char* path = vtfs_get_full_path(filp->f_path.dentry, buf, 1024);
  int res = vtfs_http_call("idk", "file/write", buf, 1024, 3, "path", path, "text", text, "offset", offsetbuf);

  // printk(KERN_INFO "Fiel path (write): %s, offset: %lld, len %ld; cop_result: %d; http_resulr: %d; result: %s\n", path, *offset, len, cop, res, buf);

  
  if (res != 0 || buf[2] != '1') {
    kfree(text);
    return -1;
  }

  kfree(text);
  return len;
}


static int __init vtfs_init(void) {
  LOG("VTFS joined the kernel\n");
  return register_filesystem(&vtfs_fs_type);
}

static void __exit vtfs_exit(void) {
  LOG("VTFS left the kernel\n");
  unregister_filesystem(&vtfs_fs_type);
}

module_init(vtfs_init);
module_exit(vtfs_exit);
