package com.filesystem.fs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.filesystem.fs.model.Code;
import com.filesystem.fs.model.Link;

@RestController
@RequestMapping("/filesystem/link")
public class LinkController {

    @GetMapping("/create")
    public ResponseEntity<String> createLink(
        @RequestParam(value = "path", required = true) String path,
        @RequestParam(value = "filePath", required = true) String filePath,
        @RequestParam(value = "name", required = true) String name) 
    {
        Code code = Link.createLink(path, filePath, name);
        String content = ResponseFormatter.format(code.isOk(), code.getDesc(), null, null);

        return ResponseEntity.status(200).body(content);
    }
}
