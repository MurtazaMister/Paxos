package com.lab.paxos.networkObjects.communique;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sync implements Serializable{
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private Long lastCommittedTransactionBlockId;

    private String lastCommittedTransactionBlockHash;

    List<Long> listNodesWithLatestLog;
}