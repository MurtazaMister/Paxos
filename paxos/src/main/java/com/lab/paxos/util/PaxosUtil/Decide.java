package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.model.UserAccount;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.SocketMessageUtil;
import com.lab.paxos.util.Stopwatch;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
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

    public void decide(int assignedPort, int ballotNumber, TransactionBlock transactionBlock) {
        List<Integer> portsArray = portUtil.portPoolGenerator();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("Sending decide message and then performing transactions");

        try {
            com.lab.paxos.networkObjects.communique.Decide decide = com.lab.paxos.networkObjects.communique.Decide.builder()
                    .ballotNumber(ballotNumber)
                    .transactionBlock(transactionBlock)
                    .build();

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

                // CODE TO COMMIT BLOCK ON THIS SERVER

                int currentClientId = assignedPort - portsArray.get(0) + 1;

                for(Transaction transaction : transactionBlock.getTransactions()){

                    if((!transaction.getIsMine()) || (transaction.getIsMine() && (transaction.getSenderId() != currentClientId))) {

                        // Execute transactions
                        //      If isMine = true
                        //          Execute the transactions where sender id != current id
                        //      If isMine = false
                        //          Execute all transactions

                        UserAccount senderAccount = userAccountRepository.findById(transaction.getSenderId()).orElse(null);
                        UserAccount receiverAccount = userAccountRepository.findById(transaction.getReceiverId()).orElse(null);

                        if(senderAccount == null || receiverAccount == null){ continue; }

                        long senderBalance = senderAccount.getBalance() - transaction.getAmount();
                        senderAccount.setBalance(senderBalance);
                        senderAccount.setEffectiveBalance(senderBalance);

                        long receiverBalance = receiverAccount.getBalance() + transaction.getAmount();
                        receiverAccount.setBalance(receiverBalance);
                        receiverAccount.setEffectiveBalance(receiverBalance);

                        userAccountRepository.save(senderAccount);
                        userAccountRepository.save(receiverAccount);

                    }

                }

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
