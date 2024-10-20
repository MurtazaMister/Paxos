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
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
//    private String latestCommittedTransactionBlockHash = "";
//    @Value("${server.transaction.commit.retry}")
//    private long serverTransactionCommitRetry;
//    private long commitRetryCounter = serverTransactionCommitRetry;

    @Autowired
    @Lazy
    private UserAccountRepository userAccountRepository;

    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;

    @Autowired
    @Lazy
    private TransactionBlockRepository transactionBlockRepository;

    @PersistenceContext
    private EntityManager entityManager;

//    @PostConstruct
//    public void init(){
//        TransactionBlock transactionBlock = transactionBlockRepository.findTopByOrderByIdxDesc();
//        if(transactionBlock != null){
//            latestCommittedTransactionBlockHash = transactionBlock.getHash();
//        }
//    }

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

    public void update(int assignedPort, String lastCommittedTransactionBlockHash, String highestCommittedTransactionBlockHash, List<Integer> PORT_POOL) throws IOException {
        update.update(assignedPort, lastCommittedTransactionBlockHash, highestCommittedTransactionBlockHash, PORT_POOL);
    }

    public void ackUpdate(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        ackUpdate.ackUpdate(assignedPort, in, out, socketMessageWrapper);
    }

    public TransactionBlock saveTransactionsFromBlock(int assignedPort, List<Integer> portsArray, TransactionBlock transactionBlock){
        String hash = transactionBlock.calculateHash();
//        if(latestCommittedTransactionBlockHash.equals(hash)){
//            log.warn("Transaction block: {} just committed, counter: {}", transactionBlock, commitRetryCounter-1);
//            commitRetryCounter--;
//            if(commitRetryCounter <= 0){
//                commitRetryCounter = serverTransactionCommitRetry;
//                TransactionBlock temp = transactionBlockRepository.findTopByOrderByIdxDesc();
//                log.warn("Commit retries exceeded, resetting latestCommittedTransactionBlockHash = {}", (temp!=null)?temp.getHash():null);
//                latestCommittedTransactionBlockHash = (temp!=null)?temp.getHash():latestCommittedTransactionBlockHash;
//            }
//            return transactionBlock;
//        }
//        else {
//            TransactionBlock check = transactionBlockRepository.findByHash(hash).orElse(null);
//
//            if(check != null){
//                log.warn("Transaction block: {} already exists, skipping", check.getHash());
//                return check;
//            }
//        }
//        commitRetryCounter = serverTransactionCommitRetry;
        TransactionBlock check = transactionBlockRepository.findByHash(hash).orElse(null);
        if(check != null){
            log.warn("Transaction block: {} already exists, skipping", check.getHash());
            return check;
        }

        performBlockTransactions(assignedPort, portsArray, transactionBlock);


        transactionBlock.setIdx(null);
        transactionBlock = transactionBlockRepository.saveAndFlush(transactionBlock);
        entityManager.clear();
//        latestCommittedTransactionBlockHash = transactionBlock.calculateHash();
        log.info("Saving {}", transactionBlock.getHash());

        return transactionBlock;

    }

    public void performBlockTransactions(int assignedPort, List<Integer> portsArray, TransactionBlock transactionBlock){
        int currentClientId = assignedPort - portsArray.get(0) + 1;

        int updatedRows = 0;

        for(Transaction transaction : transactionBlock.getTransactions()) {
            if(transaction.getSenderId() != currentClientId) {
                updatedRows = userAccountRepository.performTransaction(transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount());
            }
        }

        List<Transaction> toDelete = transactionBlock.getTransactions()
                .stream()
                .filter(transaction -> transaction.getSenderId() == currentClientId)
                .toList();

        if(!toDelete.isEmpty()){
            transactionRepository.deleteAll(toDelete);
        }
        log.info("Block transactions reflected");
    }

//    @Async
//    public void safetyChecks(TransactionBlock transactionBlock){
//        if(transactionBlockRepository.countByHash(transactionBlock.getHash()) < 2) {
//            log.info("Safety checks performed, all clear");
//            return;
//        }
//
//        transactionBlockRepository.delete(transactionBlock);
//
//        log.warn("Found blocks with same hashes {}, rolling back the latest one, id = {}", transactionBlock.getHash(), transactionBlock.getIdx());
//        int updatedRows;
//        for(Transaction transaction : transactionBlock.getTransactions()) {
//            updatedRows = userAccountRepository.performTransaction(transaction.getReceiverId(), transaction.getSenderId(), transaction.getAmount());
//        }
//    }

}
