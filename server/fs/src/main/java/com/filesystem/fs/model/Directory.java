package com.filesystem.fs.model;

import java.util.LinkedList;

public class Directory extends FsEntity {
    public static final Directory ROOT;

    private LinkedList<FsEntity> entries;

    private Directory() {
        super(FsEntity.Type.DIR);
        entries = new LinkedList<>();
    };

    static { // create root dir
        ROOT = new Directory();
        ROOT.name = "/";
        ROOT.parent = ROOT;
    }

    public static Directory create(String name, Directory parent) {
        Directory ret = new Directory();
        ret.name = name;
        ret.parent = parent;
        if (!parent.addEntry(ret)) {
            return null;
        }
        return ret;
    }

    public boolean addEntry(FsEntity entry) {
        for (FsEntity fsEntity : entries) {
            if (fsEntity.name.equals(entry.name)) return false;
        }
        entries.add(entry);
        return true;
    }

    public FsEntity getEntryByName(String name) {
        if (".".equals(name)) return this;
        if ("..".equals(name)) return parent;

        for (FsEntity fsEntity : entries) {
            if (fsEntity.name.equals(name)) {
                return fsEntity; // found
            }
        }
        return null; // not found
    }

    // May return file or link or directory
    public FsEntity getEntryByPath(String path) {
        String[] names = path.split("/");
        FsEntity ret = this;
        for (String name : names) {
            if ("".equals(name)) continue; // todo: find better fix
            if (ret instanceof Directory) {
                ret = ((Directory)ret).getEntryByName(name);
                if (ret == null) return null; // name not found
            } else {
                return null; // cant iterate file
            }
        }
        return ret; // found all
    }

    public boolean removeEntry(FsEntity entry) {
        return entries.remove(entry);
    }
    
    @Override
    public boolean delete() {
        if (!entries.isEmpty()) return false; // we can delete only empty dirs
        return super.delete();
    }

    // ---------------------- methods for controller ----------------------


    public static Code createDirectory(String absPath, String name) {
        FsEntity dir = ROOT.getEntryByPath(absPath);
        if (dir == null) return Code.NOT_EXISTS;

        if (dir instanceof Directory) {
            if (Directory.create(name, (Directory) dir) == null) {
                return Code.NAME_EXISTS;
            }
            return Code.OK;
        } else {
            return Code.NOT_A_DIRECTORY;
        }
    }

    public static Code deleteDirectory(String absPath) {
        FsEntity dir = ROOT.getEntryByPath(absPath);
        if (dir == null) return Code.NOT_EXISTS;

        if (dir instanceof Directory) {
            if (dir == ROOT) return Code.PERMISSION_DENIED; // nope
            if (dir.delete()) {
                return Code.OK;
            }
            return Code.ERROR;
        } else {
            return Code.NOT_A_DIRECTORY;
        }
    }

    public static String getDirectoryContents(String absPath) {
        FsEntity dir = ROOT.getEntryByPath(absPath);
        StringBuilder sb = new StringBuilder();
        if (dir instanceof Directory) {
            for (FsEntity fsEntity : ((Directory)dir).entries) {
                sb.append(fsEntity.type.getTypeCode());
                sb.append(",");
                sb.append(fsEntity.name);
                sb.append(",");
            }
            sb.append("2,.,2,..");
            return sb.toString();
        } else {
            return null; // not a directory
        }
    }

    public static String getEntryInfo(String absPath) {
        FsEntity entry = ROOT.getEntryByPath(absPath);
        StringBuilder sb = new StringBuilder();
        if (entry != null) {
            sb.append(entry.type.getTypeCode());
            return sb.toString();
        } else {
            return null; // not a directory
        }
    }
}
