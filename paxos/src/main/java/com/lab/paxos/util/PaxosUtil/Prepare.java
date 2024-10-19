package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.repository.TransactionBlockRepository;
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
public class Prepare {
    @Autowired
    @Lazy
    PaxosService paxosService;
    @Autowired
    @Lazy
    SocketMessageUtil socketMessageUtil;
    @Autowired
    @Lazy
    TransactionBlockRepository transactionBlockRepository;

    @Value("${server.population}")
    int serverPopulation;

    @Value("${paxos.prepare.delay}")
    private int prepareDelay;
    @Value("${paxos.prepare.range}")
    private int prepareDelayRange;
    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;

    public void prepare(int assignedPort) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Paxos initiated on port {}", assignedPort);
//        if(paxosService.getLastBallotNumberUpdateTimestamp() + delay >= System.currentTimeMillis()) {
//            try {
//                Thread.sleep(delay - (System.currentTimeMillis() - paxosService.getLastBallotNumberUpdateTimestamp()));
//                log.warn("Ongoing paxos round anticipated, delaying for a few ms");
//            } catch (InterruptedException e) {
//                log.error("Exception: {}", e.getMessage());
//            }
//        }
        log.info("Sending prepare messages");
        int ballotNumber = paxosService.getBallotNumber()+1;
        paxosService.setBallotNumber(ballotNumber);
        try{
            TransactionBlock lastCommittedTransactionBlock = transactionBlockRepository.findTopByOrderByIdxDesc();
            Long lastCommittedTransactionBlockId = transactionBlockRepository.count();
            String lastCommittedTransactionBlockHash = (lastCommittedTransactionBlock!=null)?lastCommittedTransactionBlock.getHash():null;

            com.lab.paxos.networkObjects.communique.Prepare prepare = com.lab.paxos.networkObjects.communique.Prepare.builder()
                    .ballotNumber(ballotNumber)
                    .lastCommittedTransactionBlockId(lastCommittedTransactionBlockId)
                    .lastCommittedTransactionBlockHash(lastCommittedTransactionBlockHash)
                    .build();

            SocketMessageWrapper socketMessageWrapper = SocketMessageWrapper.builder()
                    .type(SocketMessageWrapper.MessageType.PREPARE)
                    .prepare(prepare)
                    .fromPort(assignedPort)
                    .build();

            try{
                List<AckMessageWrapper> ackMessageWrapperList = socketMessageUtil.broadcast(socketMessageWrapper).get();
                log.info("Received promise from {} servers", ackMessageWrapperList.size());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Prepare"));

                Long highestCommittedTransactionBlockId = lastCommittedTransactionBlockId;
                String highestCommittedTransactionBlockHash = lastCommittedTransactionBlockHash;

                for(AckMessageWrapper ackMessageWrapper : ackMessageWrapperList){
                    if(ackMessageWrapper.getPromise().getLastCommittedTransactionBlockId() > highestCommittedTransactionBlockId){
                        highestCommittedTransactionBlockId = ackMessageWrapper.getPromise().getLastCommittedTransactionBlockId();
                        highestCommittedTransactionBlockHash = ackMessageWrapper.getPromise().getLastCommittedTransactionBlockHash();
                    }
                }

                List<Integer> listNodesWithLatestLog = new ArrayList<>();
                List<Integer> listNodesWithoutLatestLog = new ArrayList<>();

                long transaction_count = transactionRepository.count();

                for(AckMessageWrapper ackMessageWrapper : ackMessageWrapperList){
                    transaction_count += (ackMessageWrapper.getPromise().getTransactions()!=null)?ackMessageWrapper.getPromise().getTransactions().size():0;
                    if(ackMessageWrapper.getPromise().getLastCommittedTransactionBlockId().equals(highestCommittedTransactionBlockId)){
                        listNodesWithLatestLog.add(ackMessageWrapper.getFromPort());
                    }
                    else{
                        log.info("Sending SYNC to {}, lastCommittedTransactionBlock: {}", ackMessageWrapper.getFromPort(), ackMessageWrapper.getPromise().getLastCommittedTransactionBlockId());
                        listNodesWithoutLatestLog.add(ackMessageWrapper.getFromPort());
                    }
                }
                if(highestCommittedTransactionBlockId.equals(lastCommittedTransactionBlockId))
                    listNodesWithLatestLog.add(assignedPort);

                log.warn("Highest committed transaction block: {}, hID: {}, hHash: {}", lastCommittedTransactionBlock, highestCommittedTransactionBlockId, highestCommittedTransactionBlockHash);
                log.info("Highest committed transaction block: {}, with servers: {}", highestCommittedTransactionBlockId, listNodesWithLatestLog);

                // Send SYNC
                if(!listNodesWithoutLatestLog.isEmpty()) paxosService.sync(assignedPort, ballotNumber, highestCommittedTransactionBlockId, highestCommittedTransactionBlockHash, listNodesWithLatestLog, listNodesWithoutLatestLog);

                if(highestCommittedTransactionBlockId.equals(lastCommittedTransactionBlockId)){
                    // Leader has the latest updated log
                    // send SYNC to those with un-updated log and move ahead with paxos

                    // If received majority votes, time to send the accept message
                    // 1 is the proposer itself, rest population/2
                    // If pop = 5, majority = 1 + 2 = 3
                    // If pop = 6, majority = 1 + 3 = 4
                    if(ackMessageWrapperList.size() >= serverPopulation/2 && transaction_count>0) {
                        // Moving on to the accept phase
                        paxosService.accept(assignedPort, ballotNumber, ackMessageWrapperList, listNodesWithLatestLog);
                    }
                    else{
                        if(transaction_count == 0){
                            log.info("No new transactions, restarting consensus");
                        }
                        // restart paxos with a higher ballot number
                        Stopwatch.randomSleep(prepareDelay - prepareDelayRange, prepareDelay + prepareDelayRange);
                        paxosService.prepare(assignedPort);
                    }

                }
                else{
                    // Leader does not have updated log
                    // send SYNC to everyone with un-updated log
                    // terminate paxos and restart

                    paxosService.update(assignedPort, lastCommittedTransactionBlockHash, highestCommittedTransactionBlockHash, listNodesWithLatestLog);

                    Stopwatch.randomSleep(prepareDelay - prepareDelayRange, prepareDelay + prepareDelayRange);
                    paxosService.prepare(assignedPort);

                }
            }
            catch(InterruptedException | ExecutionException e){
                log.error("Error while broadcasting messages: {}", e.getMessage());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Prepare"));
            }
        }
        catch(IOException e){
            log.error("IOException {}", e.getMessage());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Prepare"));
        }
        catch (Exception e){
            log.error(e.getMessage());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Prepare"));
        }
    }
}
