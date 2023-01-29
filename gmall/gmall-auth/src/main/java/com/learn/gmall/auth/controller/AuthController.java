package com.learn.gmall.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam("returnUrl")String returnUrl, Model model){
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }
}
