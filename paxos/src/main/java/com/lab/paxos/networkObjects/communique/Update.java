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
public class Update implements Serializable {
    private static final long serialVersionUID = 1L;

    // (startId, endId]

    private String lastCommittedTransactionBlockHash; // Value until which the sender has blocks
    // Start from startId + 1

    private String highestCommittedTransactionBlockHash; // Until endId

}
