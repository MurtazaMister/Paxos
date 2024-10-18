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
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

//    private final ExecutorService executorService = Executors.newCachedThreadPool();

//    public ResponseEntity<String> processTransactionAsync(TransactionDTO transactionDTO) {
//        CompletableFuture<ResponseEntity<Transaction>> futureTransaction = CompletableFuture
//                .supplyAsync(() -> processTransaction(transactionDTO), executorService)
//                .thenApplyAsync(transactionResponse -> {
//                    if(transactionResponse.getStatusCode() == HttpStatus.OK) {
//                        log.info("Transaction successfully processed: {}", transactionResponse.getBody());
//                        return ResponseEntity.ok(transactionResponse.getBody());
//                    }
//                    else{
//                        log.error("Transaction dropped: {}", transactionResponse.getBody());
//                        return ResponseEntity.badRequest().body(transactionResponse.getBody());
//                    }
//                });
//
//        return ResponseEntity.ok("Transaction submitted: "+ transactionDTO);
//    }

    @PostMapping
    @Transactional
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
                updatedRows = userAccountRepository.performTransaction(sender.getId(), receiver.getId(), transactionDTO.getAmount());
                log.info("Updated rows: {}", updatedRows);
                log.info("For transaction - {} : {} -> {}", transactionDTO.getAmount(), sender.getId(), receiver.getId());
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
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }

//    @PreDestroy
//    public void shutDownExecutor() {
//        executorService.shutdown();
//    }
}
