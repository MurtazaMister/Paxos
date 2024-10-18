package com.lab.paxos.networkObjects.communique;

import com.lab.paxos.service.PaxosService;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Prepare implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private Long lastCommittedTransactionBlockId;

    private String lastCommittedTransactionBlockHash;
}
