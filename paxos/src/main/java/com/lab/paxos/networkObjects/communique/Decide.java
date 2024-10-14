package com.lab.paxos.networkObjects.communique;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Decide implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private String blockHash;
}
