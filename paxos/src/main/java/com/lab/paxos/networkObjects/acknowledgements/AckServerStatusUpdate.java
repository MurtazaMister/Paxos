package com.lab.paxos.networkObjects.acknowledgements;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AckServerStatusUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean serverFailed;
    private int fromPort;
    private int toPort;
}
