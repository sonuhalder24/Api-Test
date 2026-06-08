package com.fresco.codelab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fresco.codelab.service.RegisterService;

@Controller
public class AuthController {

    @Autowired
    private RegisterService registerService;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "registrationpage.jsp";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "loginpage.jsp";
    }

    @PostMapping("/register")
    public String postRegister(@RequestParam("fullname") String fullname,
                               @RequestParam("username") String username,
                               @RequestParam("password") String password) {
        registerService.registerUser(fullname, username, password);
        return "loginpage.jsp";
    }
}
