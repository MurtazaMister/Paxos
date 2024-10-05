package com.lab.paxos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_account_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_account_id", nullable = false)
    private Long receiverId;

    private BigDecimal amount;

    private LocalDateTime timestamp;

    // Enum for transaction status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    public enum TransactionStatus {
        INITIALIZED, COMMITTED
    }

}
