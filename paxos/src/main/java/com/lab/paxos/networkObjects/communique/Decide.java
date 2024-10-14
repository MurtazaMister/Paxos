package com.lab.paxos.networkObjects.communique;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Decide implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private String blockHash;

    private int fromPort;
    private int toPort;
}
