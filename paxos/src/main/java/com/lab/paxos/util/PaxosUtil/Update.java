package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.repository.TransactionBlockRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.SocketMessageUtil;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class Update {

    @Autowired
    @Lazy
    private SocketMessageUtil socketMessageUtil;
    @Autowired
    @Lazy
    private PaxosService paxosService;
    @Autowired
    @Lazy
    private PortUtil portUtil;
    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;

    public void update(int assignedPort, long startId, long endId, List<Integer> PORT_POOL) {

        Random random = new Random();
        int selectedPortIndexForUpdate = random.nextInt(PORT_POOL.size());

        com.lab.paxos.networkObjects.communique.Update update = com.lab.paxos.networkObjects.communique.Update.builder()
                .startId(startId)
                .endId(endId)
                .build();

        SocketMessageWrapper socketMessageWrapper = SocketMessageWrapper.builder()
                .type(SocketMessageWrapper.MessageType.UPDATE)
                .update(update)
                .fromPort(assignedPort)
                .build();

        AckMessageWrapper ackMessageWrapper = null;

        for(int i = selectedPortIndexForUpdate, count = 0; i< PORT_POOL.size() && count< PORT_POOL.size();count++){
            socketMessageWrapper.setToPort(PORT_POOL.get(i));

            try{
                log.info("{}: Getting updates for catch-up from server {}", assignedPort, PORT_POOL.get(i));
                ackMessageWrapper = socketMessageUtil.sendMessageToServer(PORT_POOL.get(i), socketMessageWrapper);
                if(ackMessageWrapper == null) throw new IOException();
                break;
            } catch (Exception e) {
                i = (i+1) % PORT_POOL.size();
            }
        }

        if(ackMessageWrapper == null){
            log.error("Failed to update port {} with latest transaction blocks", assignedPort);
            return;
        }

        List<Integer> portsArray = portUtil.portPoolGenerator();
        List<TransactionBlock> blocks = ackMessageWrapper.getAckUpdate().getBlocks();

        for(TransactionBlock transactionBlock : blocks){
            TransactionBlock retrieved = transactionBlockRepository.findByHash(transactionBlock.getHash()).orElse(null);
            if(retrieved == null) {
                transactionBlock.setIdx(null);
                transactionBlock.setHash(null);
                paxosService.saveTransactionsFromBlock(assignedPort, portsArray, transactionBlock);
            }
        }

    }

}
