package com.lab.paxos.networkObjects.acknowledgements;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AckMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private int fromPort;
    private int toPort;
}
