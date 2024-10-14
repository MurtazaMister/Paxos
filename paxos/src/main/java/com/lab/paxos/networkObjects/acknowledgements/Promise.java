package com.lab.paxos.networkObjects.acknowledgements;

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
public class Promise implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private List<Transaction> transactions;

    private String hashPreviousCommittedBlock;

    private Integer acceptNum;

    private TransactionBlock previousTransactionBlock;
}
