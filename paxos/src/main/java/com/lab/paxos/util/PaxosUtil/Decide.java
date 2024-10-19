package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.repository.TransactionBlockRepository;
import com.lab.paxos.repository.TransactionRepository;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.SocketMessageUtil;
import com.lab.paxos.util.Stopwatch;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@Transactional
public class Decide {
    @Autowired
    @Lazy
    private PaxosService paxosService;
    @Autowired
    @Lazy
    private SocketMessageUtil socketMessageUtil;
    @Autowired
    @Lazy
    private UserAccountRepository userAccountRepository;
    @Autowired
    private PortUtil portUtil;
    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;
    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;

    public void decide(int assignedPort, int ballotNumber, TransactionBlock transactionBlock, List<Integer> listNodesWithLatestLog) {
        List<Integer> portsArray = portUtil.portPoolGenerator();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("Sending decide message and then performing transactions");

        TransactionBlock lastCommittedTransactionBlock = transactionBlockRepository.findTopByOrderByIdxDesc();
        Long lastCommittedTransactionBlockId = transactionBlockRepository.count();
        String lastCommittedTransactionBlockHash = (lastCommittedTransactionBlock!=null)?lastCommittedTransactionBlock.getHash():null;

        try {
            com.lab.paxos.networkObjects.communique.Decide decide = com.lab.paxos.networkObjects.communique.Decide.builder()
                    .ballotNumber(ballotNumber)
                    .transactionBlock(transactionBlock)
                    .lastCommittedTransactionBlockId(lastCommittedTransactionBlockId)
                    .lastCommittedTransactionBlockHash(lastCommittedTransactionBlockHash)
                    .listNodesWithLatestLog(listNodesWithLatestLog)
                    .build();

            log.info("Decide message: {}", decide);

            SocketMessageWrapper socketMessageWrapper = SocketMessageWrapper.builder()
                    .type(SocketMessageWrapper.MessageType.DECIDE)
                    .decide(decide)
                    .fromPort(assignedPort)
                    .build();

            try {
                List<AckMessageWrapper> ackMessageWrapperList = socketMessageUtil.broadcast(socketMessageWrapper).get();

                log.info("Committed on {} servers", ackMessageWrapperList.size());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Decide"));

                int currentClientId = assignedPort - portsArray.get(0) + 1;
                List<Transaction> toDelete = transactionBlock.getTransactions()
                        .stream()
                        .filter(transaction -> {
                            return transaction.getSenderId() == currentClientId;
                        })
                        .toList();

                if(!toDelete.isEmpty()){
                    transactionRepository.deleteAll(toDelete);
                }

                // CODE TO COMMIT BLOCK ON THIS SERVER

                int updatedRows = 0;

                for(Transaction transaction : transactionBlock.getTransactions()){

                    if(transaction.getSenderId() != currentClientId) {
                         updatedRows = userAccountRepository.performTransaction(transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount());
                    }

                }

                transactionBlockRepository.save(transactionBlock);

            }
            catch (InterruptedException | ExecutionException e) {
                log.error("Error while broadcasting messages: {}", e.getMessage());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Decide"));
            }
        }
        catch(IOException e) {
            log.error("IOException {}", e.getMessage());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Decide"));
        } catch (Exception e) {
            log.error("Exception {}", e.getMessage());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Decide"));
        }
    }
}
