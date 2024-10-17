package com.lab.paxos.wrapper;

import com.lab.paxos.networkObjects.acknowledgements.*;
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
        ACCEPTED,
        COMMIT
    }

    private MessageType type;
    private AckServerStatusUpdate ackServerStatusUpdate;
    private AckMessage ackMessage;
    private Promise promise;
    private Accepted accepted;
    private Commit commit;

    private int fromPort;
    private int toPort;
}
