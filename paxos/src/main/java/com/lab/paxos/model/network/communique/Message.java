package com.lab.paxos.model.network.communique;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class Message implements Serializable {
    private String message;
    private int fromPort;
    private int toPort;

    @Override
    public String toString() {
        return "Message [message = " + message + ", fromPort = " + fromPort + ", toPort = " + toPort + "]";
    }
}
