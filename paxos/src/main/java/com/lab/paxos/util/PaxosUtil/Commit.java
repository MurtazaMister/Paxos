package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.networkObjects.acknowledgements.AckMessage;
import com.lab.paxos.networkObjects.communique.Decide;
import com.lab.paxos.repository.TransactionBlockRepository;
import com.lab.paxos.repository.TransactionRepository;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.service.SocketService;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.Stopwatch;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class Commit {

    @Autowired
    @Lazy
    PaxosService paxosService;
    @Autowired
    private PortUtil portUtil;
    @Autowired
    @Lazy
    private UserAccountRepository userAccountRepository;
    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;
    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;
    @Autowired
    @Lazy
    private SocketService socketService;

    @Transactional
    public void commit(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        LocalDateTime startTime = LocalDateTime.now();
        Decide decide = socketMessageWrapper.getDecide();

        TransactionBlock transactionBlock = transactionBlockRepository.findTopByOrderByIdxDesc();
        Long lastCommittedTransactionBlockId = transactionBlockRepository.count();
        String lastCommittedTransactionBlockHash = (transactionBlock!=null)?transactionBlock.getHash():null;

        if(lastCommittedTransactionBlockId.equals(decide.getLastCommittedTransactionBlockId())) {

            log.info("Received decide: {}", decide);

            paxosService.setAcceptNum(null);
            paxosService.setPreviousTransactionBlock(null);

            List<Integer> portsArray = portUtil.portPoolGenerator();

            TransactionBlock newTransactionBlock = decide.getTransactionBlock();

            newTransactionBlock = paxosService.saveTransactionsFromBlock(assignedPort, portsArray, newTransactionBlock);

            com.lab.paxos.networkObjects.acknowledgements.Commit commit = com.lab.paxos.networkObjects.acknowledgements.Commit.builder()
                    .committedTransactionBlock(newTransactionBlock.getHash())
                    .ballotNumber(decide.getBallotNumber())
                    .build();

            AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                    .type(AckMessageWrapper.MessageType.COMMIT)
                    .commit(commit)
                    .fromPort(socketMessageWrapper.getToPort())
                    .toPort(socketMessageWrapper.getFromPort())
                    .build();

            out.writeObject(ackMessageWrapper);
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Commit"));

            log.info("Sent committed to server {}: {}", ackMessageWrapper.getToPort(), commit);
        }
        else{
            log.info("Rejecting from Commit as this server or the leader might be lagging, current transaction blk = {}, leader's = {}", lastCommittedTransactionBlockId, decide.getLastCommittedTransactionBlockId());
            LocalDateTime currentTime = LocalDateTime.now();
            log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Commit"));

//            paxosService.update(socketService.getAssignedPort(), lastCommittedTransactionBlockId, decide.getLastCommittedTransactionBlockId(), decide.getListNodesWithLatestLog());

        }
    }

}