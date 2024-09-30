package com.lab.paxos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="sender_account_id", nullable=false)
    private UserAccount sender;

    @ManyToOne
    @JoinColumn(name="receiver_account_id", nullable=false)
    private UserAccount receiver;

    private BigDecimal amount;

    private LocalDateTime timestamp;
}
