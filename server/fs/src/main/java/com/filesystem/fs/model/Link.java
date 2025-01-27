package com.filesystem.fs.model;

public class Link extends FsEntity {
    private File linkedFile;

    private Link() {
        super(FsEntity.Type.LINK);
    }

    public static Link create(String name, File linkedFile, Directory parentDir) {
        Link ret = new Link();
        ret.name = name;
        ret.linkedFile = linkedFile;
        ret.parent = parentDir;
        if (!parentDir.addEntry(ret)) {
            return null;
        }
        return ret;
    }

    public File getLinkedFile() {
        return linkedFile;
    }

    // ---------------------- methods for controller ----------------------

    public static Code createLink(String absPath, String fileAbsPath, String name) {
        FsEntity dir = Directory.ROOT.getEntryByPath(absPath);
        FsEntity file = Directory.ROOT.getEntryByPath(fileAbsPath);
        if (file == null || dir == null) return Code.NOT_EXISTS;

        if (dir instanceof Directory) {
            if (file instanceof File) {
                if (Link.create(name, (File) file, (Directory) dir) == null) {
                    return Code.NAME_EXISTS;
                }
                return Code.OK;
            } else if (file instanceof Link) {
                if (Link.create(name, ((Link)file).linkedFile, (Directory) dir) == null) {
                    return Code.NAME_EXISTS;
                }
                return Code.OK;
            } else {
                return Code.NOT_A_FILE;
            }
        } else {
            return Code.NOT_A_DIRECTORY;
        }
    }

}
