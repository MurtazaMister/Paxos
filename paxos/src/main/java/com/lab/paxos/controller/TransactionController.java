package com.lab.paxos.controller;

import com.lab.paxos.dto.TransactionDTO;
import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.UserAccount;
import com.lab.paxos.repository.TransactionRepository;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.util.ServerStatusUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
@Slf4j
public class TransactionController {

    @Autowired
    private ServerStatusUtil serverStatusUtil;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<Transaction> processTransaction(@RequestBody TransactionDTO transactionDTO) {

        if(serverStatusUtil.isFailed()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        Optional<UserAccount> optionalSender = userAccountRepository.findByUsername(transactionDTO.getUnameSender());
        Optional<UserAccount> optionalReceiver = userAccountRepository.findByUsername(transactionDTO.getUnameReceiver());

        if(optionalSender.isPresent() && optionalReceiver.isPresent()) {
            UserAccount sender = optionalSender.get();
            UserAccount receiver = optionalReceiver.get();

            if(sender.getEffectiveBalance() >= transactionDTO.getAmount()){
                Transaction transaction = Transaction.builder()
                        .amount(transactionDTO.getAmount())
                        .senderId(sender.getId())
                        .receiverId(receiver.getId())
                        .timestamp(transactionDTO.getTimestamp())
                        .status(Transaction.TransactionStatus.PENDING)
                        .build();
                transaction = transactionRepository.save(transaction);
                sender.setEffectiveBalance(sender.getEffectiveBalance() - transactionDTO.getAmount());
                userAccountRepository.save(sender);
                log.info("Performed transaction ${} : {} -> {}", transactionDTO.getAmount(), sender.getUsername(), receiver.getUsername());
                return ResponseEntity.ok(transaction);
            }
            else{
                log.info("Feature under progress");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }
}
