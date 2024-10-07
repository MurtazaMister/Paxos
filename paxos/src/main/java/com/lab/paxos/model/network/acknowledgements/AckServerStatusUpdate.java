package com.lab.paxos.model.network.acknowledgements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class AckServerStatusUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean serverFailed;
    private int fromPort;
    private int toPort;

    @Override
    public String toString() {
        return "ACK to "+toPort+" | status of "+fromPort+"= "+((serverFailed)?"failed":"up & running");
    }
}
