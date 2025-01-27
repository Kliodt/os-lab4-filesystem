package com.filesystem.fs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ! TEST ENDPOINTS FOR MY OWN PURPOSES

// http://localhost:8080/fs-0.0.1-SNAPSHOT/filesystem/dev/test?val=1234
// http://192.168.0.100:8080/fs-0.0.1-SNAPSHOT/filesystem/dev/test2

@RestController
@RequestMapping("/filesystem/dev")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> testMethod(@RequestParam(value = "val", required = true) String value) {
        return ResponseEntity.status(200).body("Hi, value is " + value + " !");
    }

    @GetMapping("/test2")
    public ResponseEntity<String> testMethod2() {
        return ResponseEntity.status(200).body("Hi!");
    }
}
