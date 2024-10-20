package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.networkObjects.communique.Prepare;
import com.lab.paxos.repository.TransactionBlockRepository;
import com.lab.paxos.repository.TransactionRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.util.Stopwatch;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class Promise {

    @Autowired
    @Lazy
    PaxosService paxosService;
    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;
    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;

    public void promise(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        LocalDateTime startTime = LocalDateTime.now();
        Prepare prepare = socketMessageWrapper.getPrepare();

        log.info("Received from port {}: {}", socketMessageWrapper.getFromPort(), prepare);

        if(prepare.getBallotNumber() > paxosService.getBallotNumber()){

            paxosService.setBallotNumber(prepare.getBallotNumber());
            paxosService.setLastBallotNumberUpdateTimestamp(System.currentTimeMillis());

            TransactionBlock lastCommittedTransactionBlock = transactionBlockRepository.findTopByOrderByIdxDesc();
            Long lastCommittedTransactionBlockId = transactionBlockRepository.countTransactionBlocks();
            String lastCommittedTransactionBlockHash = (lastCommittedTransactionBlock==null)?null:lastCommittedTransactionBlock.getHash();

            List<Transaction> transactionList = null;

            if(prepare.getLastCommittedTransactionBlockId() > lastCommittedTransactionBlockId) {
                log.info("Rejecting, transactionBlock = {}, latest = {}", lastCommittedTransactionBlockId, prepare.getLastCommittedTransactionBlockId());
            }
            else{
                transactionList = transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
            }


            // To load all the transactions that haven't yet been commited and are lying in the local log
            // to be sent to the leader

            com.lab.paxos.networkObjects.acknowledgements.Promise promise = com.lab.paxos.networkObjects.acknowledgements.Promise.builder()
                    .ballotNumber(prepare.getBallotNumber())
                    .transactions(transactionList)
                    .acceptNum(paxosService.getAcceptNum())
                    .previousTransactionBlock(paxosService.getPreviousTransactionBlock())
                    .lastCommittedTransactionBlockId(lastCommittedTransactionBlockId)
                    .lastCommittedTransactionBlockHash(lastCommittedTransactionBlockHash)
                    .build();

            AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                    .type(AckMessageWrapper.MessageType.PROMISE)
                    .promise(promise)
                    .fromPort(socketMessageWrapper.getToPort())
                    .toPort(socketMessageWrapper.getFromPort())
                    .build();

            out.writeObject(ackMessageWrapper);

            log.info("Sent promise to server {}: {}", ackMessageWrapper.getToPort(), promise);

            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Promise"));
        }
        else{
            log.info("Rejecting due to smaller ballot number, current: {}, received: {}", paxosService.getBallotNumber(), prepare.getBallotNumber());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Promise"));
        }
    }
}
