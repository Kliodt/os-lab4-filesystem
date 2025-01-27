package com.filesystem.fs.model;

public abstract class FsEntity {
    private static long nextInode = 100;

    protected long inode;
    protected String name;
    protected Directory parent;
    protected Type type;

    protected enum Type {
        FILE(1),
        DIR(2),
        LINK(3);
        private final int code;
        Type(int val) {
            code = val;
        }
        public int getTypeCode() {
            return code;
        }
    }

    protected FsEntity(Type type) {
        this.type = type;
    }


    {
        inode = nextInode++;
    }

    public String getName() {
        return name;
    }

    public long getInode() {
        return inode;
    }

    public Directory getParent() {
        return parent;
    }

    public boolean delete() {
        return parent.removeEntry(this);
    }
}
