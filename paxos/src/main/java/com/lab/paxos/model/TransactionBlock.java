package com.lab.paxos.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class TransactionBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idx;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    private String hash;

    // Enum for block status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockStatus status;

    public enum BlockStatus {
        INITIALIZED, ACCEPTED, COMMITTED
    }
}
