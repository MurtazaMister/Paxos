package com.lab.paxos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
public class ServerController {

    @GetMapping("/fail")
    public ResponseEntity<Boolean> failServer(@RequestParam(required = false) int port){
        return ResponseEntity.ok(false);
    }

    @GetMapping("/resume")
    public ResponseEntity<Boolean> resumeServer(@RequestParam(required = false) int port){
        return ResponseEntity.ok(false);
    }
}
