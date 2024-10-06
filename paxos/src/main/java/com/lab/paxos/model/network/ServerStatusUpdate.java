package com.lab.paxos.model.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class ServerStatusUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean failServer;
    private int toPort;
    private int fromPort;
}
