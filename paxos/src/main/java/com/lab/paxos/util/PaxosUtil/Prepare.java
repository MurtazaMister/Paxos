package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.service.PaxosService;
import com.lab.paxos.service.SocketService;
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
import java.time.Duration;
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

    @Value("${paxos.prepare.delay}")
    long delay;

    public void prepare(int assignedPort, PaxosService.Purpose purpose) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Paxos initiated on port {}", assignedPort);
        if(paxosService.getLastBallotNumberUpdateTimestamp() + delay >= System.currentTimeMillis()) {
            try {
                Thread.sleep(delay - (System.currentTimeMillis() - paxosService.getLastBallotNumberUpdateTimestamp()));
                log.warn("Ongoing paxos round anticipated, delaying for a few ms");
            } catch (InterruptedException e) {
                log.error("Exception: {}", e.getMessage());
            }
        }
        log.info("Sending prepare messages");
        paxosService.setBallotNumber(paxosService.getBallotNumber()+1);
        try{
            com.lab.paxos.networkObjects.communique.Prepare prepare = com.lab.paxos.networkObjects.communique.Prepare.builder()
                    .ballotNumber(paxosService.getBallotNumber())
                    .purpose(purpose)
                    .build();

            SocketMessageWrapper socketMessageWrapper = SocketMessageWrapper.builder()
                    .type(SocketMessageWrapper.MessageType.PREPARE)
                    .prepare(prepare)
                    .fromPort(assignedPort)
                    .build();

            try{
                List<AckMessageWrapper> ackMessageWrapperList = socketMessageUtil.broadcast(socketMessageWrapper).get();
                log.info("Received acknowledgements from {} servers", ackMessageWrapperList.size());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Prepare"));
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
