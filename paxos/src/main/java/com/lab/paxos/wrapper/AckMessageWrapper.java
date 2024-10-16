package com.lab.paxos.wrapper;

import com.lab.paxos.networkObjects.acknowledgements.Accepted;
import com.lab.paxos.networkObjects.acknowledgements.AckMessage;
import com.lab.paxos.networkObjects.acknowledgements.AckServerStatusUpdate;
import com.lab.paxos.networkObjects.acknowledgements.Promise;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Builder
@Setter
@Data
public class AckMessageWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        ACK_SERVER_STATUS_UPDATE,
        ACK_MESSAGE,
        PROMISE,
        ACCEPTED
    }

    private MessageType type;
    private AckServerStatusUpdate ackServerStatusUpdate;
    private AckMessage ackMessage;
    private Promise promise;
    private Accepted accepted;

    private int fromPort;
    private int toPort;
}
