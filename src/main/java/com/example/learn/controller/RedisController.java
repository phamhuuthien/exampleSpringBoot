package com.example.learn.controller;

import com.example.learn.service.BaseRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {
    private final BaseRedisService baseRedis;
    @PostMapping
    public void set(){
        baseRedis.set("hello","pham huu thien");
    }
}
