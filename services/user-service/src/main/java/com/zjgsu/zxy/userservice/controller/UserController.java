package com.zjgsu.zxy.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    public String getUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return "User " + id + " info (Mock)";
    }
}