package com.price_tracker.saas.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/status")
    public String checarStatus() {
        return "🚀 O Price Tracker SaaS está online e blindado!";
    }
}