package com.lab.paxos.service;

import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.util.PaxosUtil.Promise;
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
    Promise promise;

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

    public void accept(Purpose purpose){

    }

    public void decide(Purpose purpose){

    }

}
