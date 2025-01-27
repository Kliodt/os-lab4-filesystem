package com.filesystem.fs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.filesystem.fs.model.Directory;
import com.filesystem.fs.model.FsEntity;

@RestController
@RequestMapping("/filesystem/inode")
public class InodeController {

    @GetMapping("/get_root_inode")
    public ResponseEntity<String> getPath() 
    {
        String content = Long.toString(Directory.ROOT.getInode());
        return ResponseEntity.status(200).body(content);
    }


    @GetMapping("/get_ino")
    public ResponseEntity<String> getInode(
        @RequestParam(value = "path", required = true) String path)
    {
        String content = "-1";
        FsEntity e = Directory.ROOT.getEntryByPath(path);
        if (e != null) {
            content = Long.toString(e.getInode());
        }

        return ResponseEntity.status(200).body(content);
    }

}