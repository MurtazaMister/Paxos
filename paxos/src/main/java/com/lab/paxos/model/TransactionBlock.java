package com.lab.paxos.model;

import com.lab.paxos.util.TransactionListConverterUtil;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idx;

    @Column(columnDefinition = "MEDIUMTEXT")
    @Convert(converter = TransactionListConverterUtil.class)
    private List<Transaction> transactions;

    private String hash;

    public String calculateHash() {
        try{

            String data = transactions.stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(","));

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
