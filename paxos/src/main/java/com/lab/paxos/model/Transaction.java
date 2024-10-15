package com.lab.paxos.model;

import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_account_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_account_id", nullable = false)
    private Long receiverId;

    private Long amount;

    private LocalDateTime timestamp; // provided by the client!

    // Will be used primarily for uninitialized (missed) transactions, to compare
    private String hash; // Without including the 'id' field as it may differ across servers

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    public enum TransactionStatus {
        UNINITIALIZED, // Not current server's transaction but keeping it for broadcasting when that server is up
        SERVED, // Not current server's transaction, but has been served to the respected server via the major block
        PENDING, // Current server's transaction that is yet to be committed
        COMMITTED // Committed to the major block
    }

    public String calculateHash() {
        try{

            String data = senderId+":"+receiverId+":"+amount+":"+timestamp+":"+status;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }

    @PrePersist
    @PreUpdate
    public void saveWithHash(){
        this.hash = calculateHash();
    }

}
