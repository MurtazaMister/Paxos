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
                log.info("Received ACK from server {}: {}", ackMessageWrapper.getFromPort(), ackMessageWrapper.getAckServerStatusUpdate());
                break;

            case ACK_MESSAGE:
                log.info("Received ACK from server {}: {}", ackMessageWrapper.getFromPort(), ackMessageWrapper.getAckMessage());
                break;

            case PROMISE:
                log.info("Received Promise from server {}: {}", ackMessageWrapper.getFromPort(), ackMessageWrapper.getPromise());
                break;

            case ACCEPTED:
                log.info("Received Accepted from server {}: {}", ackMessageWrapper.getFromPort(), ackMessageWrapper.getAccepted());
                break;

            case COMMIT:
                log.info("Received Commit from server {}: {}", ackMessageWrapper.getFromPort(), ackMessageWrapper.getCommit());
                break;

            case ACK_SYNC:
                log.info("Received ACK_SYNC from server {}: {}", ackMessageWrapper.getFromPort(), ackMessageWrapper.getAckSync());
                break;

            default:
                log.error("Received unexpected ACK from server {}", ackMessageWrapper);
        }
    }
}
