package com.lab.paxos.util.PaxosUtil;

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

    @Value("${server.population}")
    int serverPopulation;

    @Value("${paxos.prepare.delay}")
    private int prepareDelay;
    @Value("${paxos.prepare.range}")
    private int prepareDelayRange;

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
            com.lab.paxos.networkObjects.communique.Prepare prepare = com.lab.paxos.networkObjects.communique.Prepare.builder()
                    .ballotNumber(ballotNumber)
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

                // If received majority votes, time to send the accept message
                // 1 is the proposer itself, rest population/2
                // If pop = 5, majority = 1 + 2 = 3
                // If pop = 6, majority = 1 + 3 = 4
                if(ackMessageWrapperList.size() >= serverPopulation/2) {
                    // Moving on to the accept phase
                    paxosService.accept(assignedPort, ballotNumber, ackMessageWrapperList);
                }
                else{
                    // restart paxos with a higher ballot number
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
