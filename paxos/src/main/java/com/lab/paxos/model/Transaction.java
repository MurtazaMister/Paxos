package com.lab.paxos.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
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
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

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

    private Boolean isMine;

    public String calculateHash() {
        try{

            String data = senderId+":"+receiverId+":"+amount+":"+timestamp;

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
