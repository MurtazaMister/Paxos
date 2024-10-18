package com.lab.paxos.networkObjects.communique;

import com.lab.paxos.model.TransactionBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accept implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private TransactionBlock block;

    private Long lastCommittedTransactionBlockId;

    private String lastCommittedTransactionBlockHash;

    List<Integer> listNodesWithLatestLog;
}
