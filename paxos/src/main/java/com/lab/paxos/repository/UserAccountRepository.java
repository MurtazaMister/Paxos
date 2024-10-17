package com.lab.paxos.repository;

import com.lab.paxos.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);

    @Procedure(name = "performTransaction")
    @Transactional
    int performTransaction(
            @Param("p_senderId") Long senderId,
            @Param("p_receiverId") Long receiverId,
            @Param("p_amount") Long amount,
            @Param("p_updatedRows") Integer updatedRows
    );
}
