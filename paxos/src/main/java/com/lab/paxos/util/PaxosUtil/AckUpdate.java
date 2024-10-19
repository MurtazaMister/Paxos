package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.networkObjects.communique.Update;
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
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AckUpdate {
    @Autowired
    @Lazy
    private PaxosService paxosService;
    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;

    public void ackUpdate(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        Update update = socketMessageWrapper.getUpdate();

        long startId = update.getStartId();
        long endId = update.getEndId();

        List<TransactionBlock> transactionBlockList = new ArrayList<>();
        TransactionBlock transactionBlock = null;
        for(long i = startId + 1; i<=endId ; i++ ){
            transactionBlock = transactionBlockRepository.getByIdx(i);
            if(transactionBlock!=null) transactionBlockList.add(transactionBlock);
        }

        com.lab.paxos.networkObjects.acknowledgements.AckUpdate ackUpdate = com.lab.paxos.networkObjects.acknowledgements.AckUpdate.builder()
                .blocks(transactionBlockList)
                .build();

        AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                .type(AckMessageWrapper.MessageType.ACK_UPDATE)
                .ackUpdate(ackUpdate)
                .fromPort(assignedPort)
                .toPort(socketMessageWrapper.getFromPort())
                .build();

        out.writeObject(ackMessageWrapper);

        log.info("Sent catch-up transaction blocks to server {} from server {}", socketMessageWrapper.getFromPort(), assignedPort);
    }
}
