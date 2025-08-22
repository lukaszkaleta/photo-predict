package com.lk.photopredict.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class PingController {
    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("Received ping request");
        return ResponseEntity.ok("pong");
    }
}
