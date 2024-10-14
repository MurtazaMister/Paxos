package com.lab.paxos.util;

import com.lab.paxos.networkObjects.communique.Prepare;
import com.lab.paxos.service.SocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaxosUtil {

    public enum Purpose{
        SYNC,
        AGGREGATE
    }

    @Autowired
    private SocketService socketService;

    @Autowired
    private PortUtil portUtil;

    private int ballotNumber = 0;

    public void prepare(Purpose purpose){

//        Prepare prepare = Prepare.

    }

    public void accept(Purpose purpose){

    }

    public void decide(Purpose purpose){

    }

}
