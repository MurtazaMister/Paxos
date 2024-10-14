package com.lab.paxos.networkObjects.communique;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerStatusUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean failServer;
    private int toPort;
    private int fromPort;
}
