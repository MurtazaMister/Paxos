package com.lab.paxos.networkObjects.communique;

import com.lab.paxos.model.TransactionBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accept implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;

    private TransactionBlock block;

    private int fromPort;
    private int toPort;
}
