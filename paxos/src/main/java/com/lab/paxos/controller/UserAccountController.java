package com.lab.paxos.controller;

import com.lab.paxos.dto.ValidateUserDTO;
import com.lab.paxos.model.UserAccount;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.util.ServerStatusUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserAccountController {
    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    ServerStatusUtil serverStatusUtil;

    @GetMapping("/getId")
    public ResponseEntity<Long> getUserIdByUsername(@RequestParam String username) {

        if(serverStatusUtil.isFailed()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        log.info("getUserIdByUsername(username) called with username: {}", username);

        Optional<UserAccount> optionalUserAccount = userAccountRepository.findByUsername(username);
        if(optionalUserAccount.isPresent()) {
            return ResponseEntity.ok(optionalUserAccount.get().getId());
        }
        return ResponseEntity.notFound().build();

    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateUser(@RequestBody ValidateUserDTO bodyUserAccount) {

        if(serverStatusUtil.isFailed()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        log.info("validateUser(userAccount) called with userId: {}", bodyUserAccount.getUserId());

        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(bodyUserAccount.getUserId());

        if(optionalUserAccount.isPresent()){

            UserAccount userAccount = optionalUserAccount.get();

            boolean isValid = userAccount.getPassword().equals(bodyUserAccount.getPassword());

            return ResponseEntity.ok(isValid);

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @GetMapping("/balance")
    public ResponseEntity<Long> balanceCheck(@RequestParam Long userId){

        if(serverStatusUtil.isFailed()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        log.info("balanceCheck(userId) called with id: {}", userId);

        UserAccount userAccount = userAccountRepository.findById(userId).orElse(null);

        if(userAccount != null){
            return ResponseEntity.ok(userAccount.getBalance());
        }

        return ResponseEntity.notFound().build();

    }
}
