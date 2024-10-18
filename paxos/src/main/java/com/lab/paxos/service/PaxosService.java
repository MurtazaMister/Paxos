package com.lab.paxos.service;

import com.lab.paxos.model.TransactionBlock;
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

    private int ballotNumber = 0;
    private long lastBallotNumberUpdateTimestamp = 0;
    private Integer acceptNum = null;
    private TransactionBlock previousTransactionBlock = null;
    @Value("${server.resume.timeout}")
    private int serverResumeDelay;

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

    public void accept(int assignedPort, int ballotNumber, List<AckMessageWrapper> promiseAckMessageWrapperList) {
        accept.accept(assignedPort, ballotNumber, promiseAckMessageWrapperList);
    }

    public void accepted(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        accepted.accepted(in, out, socketMessageWrapper);
    }

    public void decide(int assignedPort, int ballotNumber, TransactionBlock transactionBlock) {
        decide.decide(assignedPort, ballotNumber, transactionBlock);
    }

    public void commit(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        commit.commit(assignedPort, in, out, socketMessageWrapper);
    }

}
