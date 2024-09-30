package com.lab.paxos.repository;

import com.lab.paxos.model.TransactionBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionBlockRepository extends JpaRepository<TransactionBlock, Long> {
}
