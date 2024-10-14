package com.lab.paxos.service;

import com.lab.paxos.networkObjects.communique.Message;
import com.lab.paxos.networkObjects.communique.Prepare;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.SocketMessageUtil;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class PaxosService {

    @Autowired
    SocketMessageUtil socketMessageUtil;

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

        try{
            Prepare prepare = Prepare.builder()
                    .ballotNumber(++ballotNumber)
                    .purpose(purpose)
                    .build();

            SocketMessageWrapper socketMessageWrapper = SocketMessageWrapper.builder()
                    .type(SocketMessageWrapper.MessageType.PREPARE)
                    .prepare(prepare)
                    .fromPort(socketService.getAssignedPort())
                    .build();

            int acks = socketMessageUtil.broadcast(socketMessageWrapper);
        }
        catch(IOException e){
            log.error("IOException {}", e.getMessage());
        }
        catch (Exception e){
            log.error(e.getMessage());
        }

    }

    public void accept(Purpose purpose){

    }

    public void decide(Purpose purpose){

    }

}
