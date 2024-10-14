package com.lab.paxos.networkObjects.communique;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Prepare implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private Purpose purpose;

    private int fromPort;
    private int toPort;

    public enum Purpose {
        SYNC,
        AGGREGATE
    }
}
