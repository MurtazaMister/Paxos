package com.lab.paxos.util;

import com.lab.paxos.wrapper.AckMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AckDisplayUtil {
    public void displayAcknowledgement(AckMessageWrapper ackMessageWrapper){
        switch (ackMessageWrapper.getType()){

            case ACK_SERVER_STATUS_UPDATE:
                log.info("Received ACK from server {}: {}", ackMessageWrapper.getAckServerStatusUpdate().getFromPort(), ackMessageWrapper.getAckServerStatusUpdate());
                break;

            case ACK_MESSAGE:
                log.info("Received ACK from server {}: {}", ackMessageWrapper.getAckMessage().getFromPort(), ackMessageWrapper.getAckMessage());
                break;

            default:
                log.error("Received unexpected ACK from server {}", ackMessageWrapper);
        }
    }
}
