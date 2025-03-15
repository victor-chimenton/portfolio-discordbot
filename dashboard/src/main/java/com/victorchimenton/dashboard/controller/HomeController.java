package com.victorchimenton.dashboard.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String home(Model model, OAuth2AuthenticationToken authenticationToken){
    return "index";
  }

  @GetMapping("/login")
  public String login(){
    return "login";
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model, OAuth2AuthenticationToken authenticationToken){
    if (authenticationToken == null){
      return "redirect:/login";
    }

    return "dashboard";
  }



}
