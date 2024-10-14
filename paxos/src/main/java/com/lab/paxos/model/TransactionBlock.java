package com.lab.paxos.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
public class TransactionBlock implements Serializable {

    private static final long serialVersionUID = 1L;

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
        PENDING,
        ACCEPTED,
        COMMITTED
    }

    public String calculateHash() {
        try{

            String transactionData = transactions.stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(","));

            String data = transactionData + status.toString();

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
