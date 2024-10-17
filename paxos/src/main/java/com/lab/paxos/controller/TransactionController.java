package com.lab.paxos.controller;

import com.lab.paxos.dto.TransactionDTO;
import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.UserAccount;
import com.lab.paxos.repository.TransactionRepository;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.service.SocketService;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.ServerStatusUtil;
import com.lab.paxos.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private SocketService socketService;
    @Autowired
    private PortUtil portUtil;
    @Autowired
    private PaxosService paxosService;

    @PostMapping
    public ResponseEntity<Transaction> processTransaction(@RequestBody TransactionDTO transactionDTO) {

//        if(serverStatusUtil.isFailed()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        Optional<UserAccount> optionalSender = userAccountRepository.findByUsername(transactionDTO.getUnameSender());
        Optional<UserAccount> optionalReceiver = userAccountRepository.findByUsername(transactionDTO.getUnameReceiver());

        if(optionalSender.isPresent() && optionalReceiver.isPresent()) {
            UserAccount sender = optionalSender.get();
            UserAccount receiver = optionalReceiver.get();

            if(sender.getId().equals(receiver.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            if(socketService.getAssignedPort()- portUtil.basePort()+1 == sender.getId()){
                // If this transaction belongs to this server's client
                // i.e. port matches client id

                int updatedRows = 0;
                updatedRows = userAccountRepository.performTransaction(sender.getId(), receiver.getId(), transactionDTO.getAmount(), updatedRows);
                log.info("Updated rows: {}", updatedRows);

                if(updatedRows != 0) {
                    Transaction transaction = Transaction.builder()
                            .amount(transactionDTO.getAmount())
                            .senderId(sender.getId())
                            .receiverId(receiver.getId())
                            .timestamp(transactionDTO.getTimestamp())
                            .build();
                    transaction = transactionRepository.save(transaction);
                    return ResponseEntity.ok(transaction);
                }
                else{
                    log.info("Calling paxos service");
                    LocalDateTime startTime = LocalDateTime.now();

                    paxosService.prepare(socketService.getAssignedPort(), PaxosService.Purpose.AGGREGATE);

                    LocalDateTime currentTime = LocalDateTime.now();
                    log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Paxos"));

                    // Paxos complete

                    // Run process again
                    //      If sufficient funds -> executed
                    //      else paxos again
                    return processTransaction(transactionDTO);
                }
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

//            // Comment from here
//
//            if(sender.getBalance() >= transactionDTO.getAmount()){
//
//                Transaction transaction = Transaction.builder()
//                        .amount(transactionDTO.getAmount())
//                        .senderId(sender.getId())
//                        .receiverId(receiver.getId())
//                        .timestamp(transactionDTO.getTimestamp())
//                        .build();
//
//                if(socketService.getAssignedPort()- portUtil.basePort()+1 == sender.getId()){
//                    transaction.setIsMine(true);
//                    sender.setBalance(sender.getBalance() - transactionDTO.getAmount());
//                    userAccountRepository.save(sender);
//                    receiver.setBalance(receiver.getBalance() + transactionDTO.getAmount());
//                    userAccountRepository.save(receiver);
//                    log.info("Performed transaction ${} : {} -> {}", transactionDTO.getAmount(), sender.getUsername(), receiver.getUsername());
//                }
//                else{
//                    transaction.setIsMine(false);
//                    log.info("Queued uninitialized transaction ${} : {} -> {}, sender unaffected", transactionDTO.getAmount(), sender.getUsername(), receiver.getUsername());
//                }
//
//                transaction = transactionRepository.save(transaction);
//                return ResponseEntity.ok(transaction);
//            }
//            else{
//                log.info("Calling paxos service");
//                LocalDateTime startTime = LocalDateTime.now();
//
//                paxosService.prepare(socketService.getAssignedPort(), PaxosService.Purpose.AGGREGATE);
//
//                LocalDateTime currentTime = LocalDateTime.now();
//                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Paxos"));
//
//                // Paxos complete
//                UserAccount updatedSender = userAccountRepository.findByUsername(transactionDTO.getUnameSender()).orElse(null);
//                if(updatedSender != null) {
//                    if(updatedSender.getBalance() >= transactionDTO.getAmount()){
//                        return processTransaction(transactionDTO);
//                    }
//                    else{
//                        log.warn("Insufficient funds for transaction ${} : {} -> {}, running paxos again", transactionDTO.getAmount(), sender.getUsername(), receiver.getUsername());
//                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//                    }
//                }
//                else{
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//                }
//
//            }
//
//            // Till here
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }
}
