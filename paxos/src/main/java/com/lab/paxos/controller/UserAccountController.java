package com.lab.paxos.controller;

import com.lab.paxos.model.UserAccount;
import com.lab.paxos.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserAccountController {
    @Autowired
    UserAccountRepository userAccountRepository;

    @GetMapping("/getId")
    public ResponseEntity<Long> getUserIdByUsername(@RequestParam String username) {
        UserAccount userAccount = userAccountRepository.findByUsername(username);
        if(userAccount != null) {
            return ResponseEntity.ok(userAccount.getId());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateUser(@RequestBody UserAccount bodyUserAccount){
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(bodyUserAccount.getId());
        if(optionalUserAccount.isPresent()){
            UserAccount userAccount = optionalUserAccount.get();

            boolean isValid = userAccount.getPassword().equals(bodyUserAccount.getPassword());

            return ResponseEntity.ok(isValid);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
}
