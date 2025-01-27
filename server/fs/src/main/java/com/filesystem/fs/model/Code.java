package com.filesystem.fs.model;

public enum Code {
    OK("great success"),
    NOT_A_FILE("not a file"),
    NOT_A_DIRECTORY("not a directory"),
    PERMISSION_DENIED("permission denied"),
    NOT_EXISTS("not exists"),
    ERROR("error"),
    NAME_EXISTS("name is not unique");

    // private final int value;
    private final String desc;

    // private static final class NextValue {
    //     private static int nextValue = 0;
    // }
    
    Code(String desc) { 
        // this.value = NextValue.nextValue++;
        this.desc = desc;
    }

    // public int getValue() {
    //     return value;
    // }

    public boolean isOk() {
        return this == Code.OK;
    }

    public String getDesc() {
        return desc;
    }

}
