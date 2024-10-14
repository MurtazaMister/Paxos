package com.lab.paxos.networkObjects.communique;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private int fromPort;
    private int toPort;
}
