package com.filesystem.fs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.filesystem.fs.model.Code;
import com.filesystem.fs.model.File;

@RestController
@RequestMapping("/filesystem/file")
public class FileController {

    @GetMapping("/create")
    public ResponseEntity<String> createFile(
        @RequestParam(value = "path", required = true) String path,
        @RequestParam(value = "name", required = true) String name) 
    {
        Code code = File.createFile(path, name);
        String content = ResponseFormatter.format(code.isOk(), code.getDesc(), null, null);

        return ResponseEntity.status(200).body(content);
    }

    @GetMapping("/unlink")
    public ResponseEntity<String> unlinkFile(
        @RequestParam(value = "path", required = true) String path) 
    {
        Code code = File.unlinkFile(path);
        String content = ResponseFormatter.format(code.isOk(), code.getDesc(), null, null);

        return ResponseEntity.status(200).body(content);
    }

    @GetMapping("/read")
    public ResponseEntity<String> readFile(
        @RequestParam(value = "path", required = true) String path,
        @RequestParam(value = "offset", required = true) int off)
    {
        String content = File.readFile(path, off);
        content = ResponseFormatter.format(content != null, null, content, null);

        return ResponseEntity.status(200).body(content);
    }

    @GetMapping("/write")
    public ResponseEntity<String> writeFile(
        @RequestParam(value = "path", required = true) String path,
        @RequestParam(value = "offset", required = true) int off, 
        @RequestParam(value = "text", required = true) String text) 
    {
        Code code = File.writeFile(path, text, off);
        String content = ResponseFormatter.format(code.isOk(), code.getDesc(), null, null);

        return ResponseEntity.status(200).body(content);
    }

}
