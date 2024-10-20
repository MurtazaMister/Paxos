package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.networkObjects.communique.Sync;
import com.lab.paxos.repository.TransactionBlockRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Component
@Slf4j
public class AckSync {

    @Autowired
    @Lazy
    private PaxosService paxosService;
    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;

    public void ackSync(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        Sync sync = socketMessageWrapper.getSync();

        log.info("Received SYNC from port {}: Ballot Number - {}", socketMessageWrapper.getFromPort(), sync.getBallotNumber());

        com.lab.paxos.networkObjects.acknowledgements.AckSync ackSync = com.lab.paxos.networkObjects.acknowledgements.AckSync.builder()
                .ballotNumber(sync.getBallotNumber())
                .build();

        AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                .type(AckMessageWrapper.MessageType.ACK_SYNC)
                .ackSync(ackSync)
                .fromPort(assignedPort)
                .toPort(socketMessageWrapper.getFromPort())
                .build();

        out.writeObject(ackMessageWrapper);

        TransactionBlock transactionBlock = transactionBlockRepository.findTopByOrderByIdxDesc();
        long lastCommittedTransactionBlockId = transactionBlockRepository.countTransactionBlocks();
        String lastCommittedTransactionBlockHash = transactionBlock.getHash();

        paxosService.update(assignedPort, lastCommittedTransactionBlockHash, sync.getLastCommittedTransactionBlockHash(), sync.getListNodesWithLatestLog());

    }
}
