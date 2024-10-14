package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.networkObjects.acknowledgements.AckMessage;
import com.lab.paxos.networkObjects.communique.Prepare;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.service.SocketService;
import com.lab.paxos.util.SocketMessageUtil;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Component
@Slf4j
public class Promise {

    @Autowired
    @Lazy
    PaxosService paxosService;

    public void promise(ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        Prepare prepare = socketMessageWrapper.getPrepare();

        log.info("Received from port {}: {}", socketMessageWrapper.getFromPort(), prepare);

        if(prepare.getBallotNumber() > paxosService.getBallotNumber()){

            paxosService.setBallotNumber(prepare.getBallotNumber());

            AckMessage ackMessage = AckMessage.builder()
                    .message("Accepted")
                    .build();

            AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                    .type(AckMessageWrapper.MessageType.ACK_MESSAGE)
                    .ackMessage(ackMessage)
                    .fromPort(socketMessageWrapper.getToPort())
                    .toPort(socketMessageWrapper.getFromPort())
                    .build();

            out.writeObject(ackMessageWrapper);

            log.info("Sent ACK to server {}: {}", ackMessageWrapper.getToPort(), ackMessage);

            out.flush();
        }
        else{
            log.info("Rejecting due to smaller ballot number, current: {}, received: {}", paxosService.getPromise(), prepare.getBallotNumber());
        }
    }
}
