package com.filesystem.fs.model;

public class File extends FsEntity {
    StringBuilder content;

    private File() {
        super(FsEntity.Type.FILE);
        content = new StringBuilder();
    }

    public static File create(String name, Directory dir) {
        File ret = new File();
        ret.name = name;
        ret.parent = dir;
        if (!dir.addEntry(ret)) {
            return null;
        }
        return ret;
    }

    public String read(int offset) {
        // todo: offset
        if (offset >= content.length()) {
            return null;
        }
        return content.toString().substring(offset);
    }

    public boolean write(String str, int offset) {
        // todo: offset
        if (offset > content.length()) {
            return false;
        }
        content.setLength(offset);
        content.append(str);
        return true;
    }

    // ---------------------- methods for controller ----------------------
    public static Code createFile(String absPath, String name) {
        FsEntity dir = Directory.ROOT.getEntryByPath(absPath);
        if (dir == null) return Code.NOT_EXISTS;

        if (dir instanceof Directory) {
            if (File.create(name, (Directory) dir) == null) {
                return Code.NAME_EXISTS;
            }
            return Code.OK;
        } else {
            return Code.NOT_A_DIRECTORY;
        }
    }

    public static Code unlinkFile(String absPath) {
        FsEntity file = Directory.ROOT.getEntryByPath(absPath);
        if (file == null) return Code.NOT_EXISTS;

        if (file instanceof File || file instanceof Link) {
            file.delete();
            return Code.OK;
        } else {
            return Code.NOT_A_FILE;
        }
    }

    public static String readFile(String absPath, int offset) {
        FsEntity file = Directory.ROOT.getEntryByPath(absPath);
        if (file instanceof File) {
            return ((File)file).read(offset);
        } else if (file instanceof Link) {
            return ((Link)file).getLinkedFile().read(offset);
        } else {
            return null; // directory
        }
    }

    public static Code writeFile(String absPath, String text, int offset) {
        FsEntity file = Directory.ROOT.getEntryByPath(absPath);
        if (file instanceof File) {
            if (((File)file).write(text, offset))
                return Code.OK;
            return Code.ERROR;
        } else if (file instanceof Link) {
            if (((Link)file).getLinkedFile().write(text, offset))
                return Code.OK;
            return Code.ERROR;
        } else {
            return Code.NOT_A_FILE;
        }
    }
}
