package com.lab.paxos.model.network.acknowledgements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class AckMessage implements Serializable {
    private String message;
    private int fromPort;
    private int toPort;

    @Override
    public String toString() {
        return "AckMessage [message = " + message + ", fromPort = " + fromPort + ", toPort = " + toPort + "]";
    }
}
