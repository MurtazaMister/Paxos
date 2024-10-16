package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.networkObjects.communique.Accept;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.util.Stopwatch;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

@Component
@Slf4j
public class Accepted {

    @Autowired
    @Lazy
    PaxosService paxosService;

    public void accepted(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        LocalDateTime startTime = LocalDateTime.now();
        Accept accept = socketMessageWrapper.getAccept();

        log.info("Received from port {}: {}", socketMessageWrapper.getFromPort(), socketMessageWrapper.getAccept());

        if(accept.getBallotNumber() >= paxosService.getBallotNumber()){

            paxosService.setBallotNumber(accept.getBallotNumber());
            paxosService.setLastBallotNumberUpdateTimestamp(System.currentTimeMillis());

            // To save the transaction block that has been
            // received
            paxosService.setPreviousTransactionBlock(accept.getBlock());
            paxosService.setAcceptNum(accept.getBallotNumber());

            com.lab.paxos.networkObjects.acknowledgements.Accepted accepted = com.lab.paxos.networkObjects.acknowledgements.Accepted.builder()
                    .ballotNumber(accept.getBallotNumber())
                    .blockHash(accept.getBlock().getHash())
                    .build();

            AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                    .type(AckMessageWrapper.MessageType.ACCEPTED)
                    .accepted(accepted)
                    .fromPort(socketMessageWrapper.getToPort())
                    .toPort(socketMessageWrapper.getFromPort())
                    .build();

            out.writeObject(ackMessageWrapper);

            log.info("Sent accepted to server {}: {}", ackMessageWrapper.getToPort(), accepted);

            out.flush();
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accepted"));
        }
        else{
            log.info("Rejecting due to smaller ballot number, current: {}, received: {}", paxosService.getBallotNumber(), accept.getBallotNumber());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accepted"));
        }
    }
}
