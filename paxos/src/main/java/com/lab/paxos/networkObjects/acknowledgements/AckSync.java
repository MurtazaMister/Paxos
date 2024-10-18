package com.lab.paxos.networkObjects.acknowledgements;

import java.io.Serializable;

public class AckSync implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ballotNumber;
}
