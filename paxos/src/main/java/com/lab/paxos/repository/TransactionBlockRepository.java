package com.lab.paxos.repository;

import com.lab.paxos.model.TransactionBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TransactionBlockRepository extends JpaRepository<TransactionBlock, Long> {
    TransactionBlock findTopByOrderByIdxDesc();

    boolean existsByHash(String hash);

    TransactionBlock getByIdx(Long id);

    Optional<TransactionBlock> findByHash(String hash);

    TransactionBlock findIdByHash(String hash);

    @Query("SELECT COUNT(t) FROM TransactionBlock t")
    Long countTransactionBlocks();

    Long countByHash(String hash);
}
