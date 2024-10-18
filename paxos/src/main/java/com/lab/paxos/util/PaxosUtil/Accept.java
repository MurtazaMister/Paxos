package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.repository.TransactionRepository;
import com.lab.paxos.service.PaxosService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class Accept {

    @Autowired
    @Lazy
    private PaxosService paxosService;
    @Autowired
    @Lazy
    private SocketMessageUtil socketMessageUtil;
    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;
    @Value("${server.population}")
    int serverPopulation;
    @Value("${paxos.prepare.delay}")
    private int prepareDelay;
    @Value("${paxos.prepare.range}")
    private int prepareDelayRange;

    public void accept(int assignedPort, int ballotNumber, List<AckMessageWrapper> promiseAckMessageWrapperList){
        // If any transaction blocks are received
            // Check for all the transaction blocks within the acknowledgements
            // and with the current server, compare the ballot numbers
        // else create a transaction block from all the transactions that have been received and collect votes

        // HANDLE SOMETHING ABOUT LAST COMMITTED TRANSACTION BLOCK HASH

        LocalDateTime startTime = LocalDateTime.now();

        try{

            TransactionBlock receivedTransactionBlock = null;
            Integer prevBallotNumber = null;

            for(AckMessageWrapper ackMessageWrapper : promiseAckMessageWrapperList){
                if(ackMessageWrapper.getPromise().getPreviousTransactionBlock() != null){
                    if(prevBallotNumber == null){
                        receivedTransactionBlock = ackMessageWrapper.getPromise().getPreviousTransactionBlock();
                        prevBallotNumber = ackMessageWrapper.getPromise().getAcceptNum();
                    }
                    else if(ackMessageWrapper.getPromise().getAcceptNum() > prevBallotNumber){
                        receivedTransactionBlock = ackMessageWrapper.getPromise().getPreviousTransactionBlock();
                        prevBallotNumber = ackMessageWrapper.getPromise().getAcceptNum();
                    }
                }
            }

            com.lab.paxos.networkObjects.communique.Accept accept = com.lab.paxos.networkObjects.communique.Accept.builder()
                    .ballotNumber(ballotNumber)
                    .build();

            String hash;

            if(receivedTransactionBlock == null){

                TransactionBlock transactionBlock = TransactionBlock.builder()
                        .transactions(new ArrayList<Transaction>())
                        .build();

                List<Transaction> myTransactions = transactionRepository.findAll();

                for(Transaction transaction : myTransactions){
                    transactionBlock.getTransactions().add(transaction);
                }

                for(AckMessageWrapper ackMessageWrapper : promiseAckMessageWrapperList){
                    for(Transaction transaction : ackMessageWrapper.getPromise().getTransactions()){
                        transactionBlock.getTransactions().add(transaction);
                    }
                }

                hash = transactionBlock.calculateHash();
                transactionBlock.setHash(hash);

                accept.setBlock(transactionBlock);

            }
            else{
                hash = receivedTransactionBlock.getHash();
                accept.setBlock(receivedTransactionBlock);
            }

            log.info("Sending accept message: {}", accept);

            SocketMessageWrapper socketMessageWrapper = SocketMessageWrapper.builder()
                    .type(SocketMessageWrapper.MessageType.ACCEPT)
                    .accept(accept)
                    .fromPort(assignedPort)
                    .build();

            try{
                List<AckMessageWrapper> ackMessageWrapperList = socketMessageUtil.broadcast(socketMessageWrapper).get();

                // Comparing hash to confirm that all servers
                // agree on the same block
                int count = 0;
                for(AckMessageWrapper ackMessageWrapper : ackMessageWrapperList){
                    if(hash.equals(ackMessageWrapper.getAccepted().getBlockHash())){
                        count++;
                    }
                }

                log.info("Received accepts from {} servers", count);
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accept"));

                if(count >= serverPopulation/2){
                    log.info("Moving on to decide phase");
                    paxosService.decide(assignedPort, ballotNumber, accept.getBlock());
                }
                else{
                    // Restart paxos with a higher ballot number
                    Stopwatch.randomSleep(prepareDelay - prepareDelayRange, prepareDelay + prepareDelayRange);
                    paxosService.prepare(assignedPort);
                }

            }
            catch(InterruptedException | ExecutionException e){
                log.error("Error while broadcasting messages: {}", e.getMessage());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accept"));
            }

        } catch (IOException e) {
            log.error("IOException {}", e.getMessage());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accept"));
        } catch (Exception e) {
            log.error("Exception {}", e.getMessage());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accept"));
        }
    }
}
