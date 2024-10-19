package com.lab.paxos.repository;

import com.lab.paxos.model.TransactionBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionBlockRepository extends JpaRepository<TransactionBlock, Long> {
    TransactionBlock findTopByOrderByIdxDesc();
    boolean existsByHash(String hash);
    TransactionBlock getByIdx(Long id);
    Optional<TransactionBlock> findByHash(String hash);
    TransactionBlock findIdByHash(String hash);
}
