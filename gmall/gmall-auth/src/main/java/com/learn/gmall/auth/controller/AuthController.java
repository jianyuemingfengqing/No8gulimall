package com.learn.gmall.auth.controller;

import com.learn.gmall.auth.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    @Resource
    private AuthService authService;

    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl", defaultValue = "http://gmall.com") String returnUrl, Model model) {
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(
            @RequestParam("returnUrl") String returnUrl,
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response
    ) {
        authService.accredit(loginName, password, request, response);
        return "redirect:" + returnUrl;
    }
}
