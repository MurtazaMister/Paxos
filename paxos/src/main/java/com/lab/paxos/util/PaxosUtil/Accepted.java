package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.networkObjects.communique.Accept;
import com.lab.paxos.repository.TransactionBlockRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.service.SocketService;
import com.lab.paxos.util.SocketMessageUtil;
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
    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;
    @Autowired
    @Lazy
    private SocketService socketService;

    public void accepted(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        LocalDateTime startTime = LocalDateTime.now();
        Accept accept = socketMessageWrapper.getAccept();

        log.info("Received from port {}: {}", socketMessageWrapper.getFromPort(), socketMessageWrapper.getAccept());

        TransactionBlock transactionBlock = transactionBlockRepository.findTopByOrderByIdxDesc();
        Long lastCommittedTransactionBlockId = (transactionBlock!=null)?transactionBlock.getIdx():0;
        String lastCommittedTransactionBlockHash = (transactionBlock!=null)?transactionBlock.getHash():null;

        if(accept.getBallotNumber() >= paxosService.getBallotNumber() && lastCommittedTransactionBlockId.equals(accept.getLastCommittedTransactionBlockId())){

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

            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accepted"));
        }
        else{
            if(accept.getBallotNumber() < paxosService.getBallotNumber()) {
                log.info("Rejecting due to smaller ballot number, current: {}, received: {}", paxosService.getBallotNumber(), accept.getBallotNumber());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accepted"));

//                paxosService.update(socketService.getAssignedPort(), lastCommittedTransactionBlockId, accept.getLastCommittedTransactionBlockId(), accept.getListNodesWithLatestLog());

            }
            else {
                log.info("Rejecting from Accepted as this server or the leader might be lagging, current transaction blk = {}, leader's = {}", lastCommittedTransactionBlockId, accept.getLastCommittedTransactionBlockId());
                LocalDateTime currentTime = LocalDateTime.now();
                log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Accepted"));
            }
        }
    }
}
