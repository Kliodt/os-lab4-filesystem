package com.filesystem.fs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.filesystem.fs.model.Code;
import com.filesystem.fs.model.Directory;

@RestController
@RequestMapping("/filesystem/dir")
public class DirController {

    @GetMapping("/get")
    public ResponseEntity<String> getDirContents(
        @RequestParam(value = "path", required = true) String path) 
    {
        String content = Directory.getDirectoryContents(path);
        content = ResponseFormatter.format(content != null, null, null, content);
        return ResponseEntity.status(200).body(content);
    }


    @GetMapping("/create")
    public ResponseEntity<String> createDir(
        @RequestParam(value = "path", required = true) String path,
        @RequestParam(value = "name", required = true) String name)
    {
        Code code = Directory.createDirectory(path, name);
        String content = ResponseFormatter.format(code.isOk(), code.getDesc(), null, null);

        return ResponseEntity.status(200).body(content);
    }


    @GetMapping("/remove")
    public ResponseEntity<String> removeDir(
        @RequestParam(value = "path", required = true) String path)
    {
        Code code = Directory.deleteDirectory(path);
        String content = ResponseFormatter.format(code.isOk(), code.getDesc(), null, null);

        return ResponseEntity.status(200).body(content);
    }

    @GetMapping("/entry_info")
    public ResponseEntity<String> getInfo(
        @RequestParam(value = "path", required = true) String path)
    {
        String content = Directory.getEntryInfo(path);
        content = ResponseFormatter.format(content != null, null, null, content);
        return ResponseEntity.status(200).body(content);
    }
    
}
