package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.GenimiService;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private GenimiService genimiService;

    @GetMapping("/hello")
    public String sayHello() {
        return genimiService.GenerateTextFromTextInput("Hello AI") ;
    }
}