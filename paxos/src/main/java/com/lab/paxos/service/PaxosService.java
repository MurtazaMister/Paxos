package com.lab.paxos.service;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.repository.TransactionBlockRepository;
import com.lab.paxos.repository.TransactionRepository;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.util.PaxosUtil.*;
import com.lab.paxos.util.ServerStatusUtil;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@Getter
@Setter
public class PaxosService {

    @Autowired
    @Lazy
    private ServerStatusUtil serverStatusUtil;

    @Autowired
    com.lab.paxos.util.PaxosUtil.Prepare prepare;

    @Autowired
    private Promise promise;

    @Autowired
    private Accept accept;

    @Autowired
    private Accepted accepted;

    @Autowired
    private Decide decide;

    @Autowired
    private Commit commit;

    @Autowired
    private Sync sync;

    @Autowired
    private Update update;

    @Autowired
    private AckSync ackSync;

    @Autowired
    private AckUpdate ackUpdate;

    private int ballotNumber = 0;
    private long lastBallotNumberUpdateTimestamp = 0;
    private Integer acceptNum = null;
    private TransactionBlock previousTransactionBlock = null;
    @Value("${server.resume.timeout}")
    private int serverResumeDelay;

    @Autowired
    @Lazy
    private UserAccountRepository userAccountRepository;

    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;

    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;

    public void prepare(int assignedPort){
        try {
            if(serverStatusUtil.isFailed()) {
                while(serverStatusUtil.isFailed()){
                        Thread.sleep(serverResumeDelay);
                        log.info("Sleeping for {}ms until server resumes", serverResumeDelay);
                }
            }
//            else{
//                log.info("Sleeping for {}ms before initiating paxos", prepareDelay);
//                Thread.sleep(prepareDelay);
//            }
        } catch (Exception e) {
            log.error("Exception while waiting for server {} to resume: {}", assignedPort, e.getMessage());
        }
        prepare.prepare(assignedPort);
    }

    public void promise(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        promise.promise(in, out, socketMessageWrapper);
    }

    public void accept(int assignedPort, int ballotNumber, List<AckMessageWrapper> promiseAckMessageWrapperList, List<Integer> listNodesWithLatestLog) {
        accept.accept(assignedPort, ballotNumber, promiseAckMessageWrapperList, listNodesWithLatestLog);
    }

    public void accepted(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        accepted.accepted(in, out, socketMessageWrapper);
    }

    public void decide(int assignedPort, int ballotNumber, TransactionBlock transactionBlock, List<Integer> listNodesWithLatestLog) {
        decide.decide(assignedPort, ballotNumber, transactionBlock, listNodesWithLatestLog);
    }

    public void commit(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        commit.commit(assignedPort, in, out, socketMessageWrapper);
    }

    public void sync(int assignedPort, int ballotNumber, Long lastCommittedTransactionBlockId, String lastCommittedTransactionBlockHash, List<Integer> listNodesWithLatestLog, List<Integer> listNodesWithoutLatestLog) throws IOException, ExecutionException, InterruptedException {
        sync.sync(assignedPort, ballotNumber, lastCommittedTransactionBlockId, lastCommittedTransactionBlockHash, listNodesWithLatestLog, listNodesWithoutLatestLog);
    }

    public void ackSync(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        ackSync.ackSync(assignedPort, in, out, socketMessageWrapper);
    }

    public void update(int assignedPort, long startId, long endId, List<Integer> PORT_POOL) throws IOException {
        update.update(assignedPort, startId, endId, PORT_POOL);
    }

    public void ackUpdate(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        ackUpdate.ackUpdate(assignedPort, in, out, socketMessageWrapper);
    }

    public TransactionBlock saveTransactionsFromBlock(int assignedPort, List<Integer> portsArray, TransactionBlock transactionBlock){
        int currentClientId = assignedPort - portsArray.get(0) + 1;

        List<Transaction> toDelete = transactionBlock.getTransactions()
                .stream()
                .filter(transaction -> transaction.getSenderId() == currentClientId)
                .toList();

        if(!toDelete.isEmpty()){
            transactionRepository.deleteAll(toDelete);
        }

        int updatedRows = 0;

        for(Transaction transaction : transactionBlock.getTransactions()) {
            if(transaction.getSenderId() != currentClientId) {
                updatedRows = userAccountRepository.performTransaction(transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount());
            }
        }

        transactionBlock = transactionBlockRepository.save(transactionBlock);

        return transactionBlock;

    }

}
