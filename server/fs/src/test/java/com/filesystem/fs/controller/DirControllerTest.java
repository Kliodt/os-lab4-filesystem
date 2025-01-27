package com.filesystem.fs.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DirControllerTest {

    @Autowired
    private DirController dirController;

    @Test
    void testDirOperations() {
        // empty fs
        System.out.println(dirController.getDirContents("/").getBody());
        System.out.println(dirController.getDirContents("/.").getBody());
        System.out.println(dirController.getDirContents("/..").getBody());
        System.out.println(dirController.getDirContents("/././.././..").getBody());
        System.out.println(dirController.getDirContents("/hello").getBody()); // err
        System.out.println("--------------------------");

        // some dirs
        System.out.println(dirController.createDir("/", "dir1"));
        System.out.println(dirController.createDir("/././.././..", "dir2"));
        System.out.println(dirController.createDir("/.", "dir3"));
        System.out.println(dirController.createDir("/hello", "err_dir1")); // err

        System.out.println(dirController.getDirContents("/").getBody());
        System.out.println(dirController.getDirContents("/.").getBody());
        System.out.println(dirController.getDirContents("/..").getBody());
        System.out.println(dirController.getDirContents("/././.././..").getBody());
        System.out.println(dirController.getDirContents("/dir1").getBody());
        System.out.println(dirController.getDirContents("/dir2").getBody());
        System.out.println(dirController.getDirContents("/dir3").getBody());
        System.out.println(dirController.getDirContents("/hello").getBody()); // err
        System.out.println("--------------------------");

        // more dirs
        System.out.println(dirController.createDir("/dir1", "dir1-1"));
        System.out.println(dirController.createDir("/dir1/dir1-1", "dir1-1-1"));
        System.out.println(dirController.createDir("/dir1/dir1-1/dir1-1-1", "dir1-final1"));
        System.out.println(dirController.createDir("/dir1/dir1-1/dir1-1-1", "dir1-final2"));

        System.out.println(dirController.getDirContents("/dir1").getBody());
        System.out.println(dirController.getDirContents("/dir2").getBody());
        System.out.println(dirController.getDirContents("/dir3").getBody());
        System.out.println(dirController.getDirContents("/dir1/dir1-1").getBody());
        System.out.println(dirController.getDirContents("/dir1/dir1-1/dir1-1-1").getBody());
        System.out.println("--------------------------");

        // remove dirs
        System.out.println(dirController.removeDir("/dir2"));
        System.out.println(dirController.getDirContents("/").getBody()); // dir1, dir3

        System.out.println(dirController.removeDir("/dir1/dir1-1/dir1-1-1/baddir")); // err
        System.out.println(dirController.getDirContents("/dir1/dir1-1/dir1-1-1").getBody());
        
        System.out.println(dirController.removeDir("/dir1/dir1-1/dir1-1-1/dir1-final1"));
        System.out.println(dirController.getDirContents("/dir1/dir1-1/dir1-1-1").getBody()); // dir1-final2

        System.out.println(dirController.removeDir("/dir1/dir1-1/dir1-1-1/dir1-final1")); // err
        System.out.println(dirController.getDirContents("/dir1/dir1-1/dir1-1-1").getBody()); // dir1-final2

        System.out.println(dirController.removeDir("/dir1/dir1-1"));

        System.out.println(dirController.getDirContents("/dir1/dir1-1/dir1-1-1").getBody()); // err
        System.out.println(dirController.getDirContents("/dir1/dir1-1").getBody()); // err
        System.out.println(dirController.getDirContents("/dir1").getBody());
        System.out.println(dirController.getDirContents("/").getBody());


    }

}
