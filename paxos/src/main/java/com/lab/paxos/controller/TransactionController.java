package com.lab.paxos.controller;

import com.lab.paxos.model.Transaction;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @GetMapping("/status")
    public String getStatus() {
        return "UP & Running";
    }

    @PostMapping("/")
    public String processTransaction(@RequestBody Transaction transaction) {
        return "Transaction Processed!";
    }

}
