package com.example.activemq.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/simple-test")
    public String test() {
        return "Simpler test";
    }
}