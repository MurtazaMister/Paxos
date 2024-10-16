package com.lab.paxos.service;

import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.util.PaxosUtil.*;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public enum Purpose{
        SYNC,
        AGGREGATE
    }

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

    public void prepare(int assignedPort, Purpose purpose){
        prepare.prepare(assignedPort, purpose);
    }

    public void promise(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        promise.promise(in, out, socketMessageWrapper);
    }

    public void accept(int assignedPort, int ballotNumber, Purpose purpose, List<AckMessageWrapper> promiseAckMessageWrapperList) {
        accept.accept(assignedPort, ballotNumber, purpose, promiseAckMessageWrapperList);
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
