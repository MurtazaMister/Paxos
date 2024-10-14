package com.lab.paxos.networkObjects.acknowledgements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Accepted implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private String blockHash;
}
