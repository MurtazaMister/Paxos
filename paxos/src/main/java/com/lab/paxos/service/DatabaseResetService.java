package com.lab.paxos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DatabaseResetService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExitService exitService;

    private String[] tables = {"transaction_block", "transaction", "transaction_block_seq"};

    @Transactional
    public void resetDatabase() {

        // Removing foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

        // Dropping all transaction tables
        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }

        jdbcTemplate.execute("INSERT INTO TRANSACTION_BLOCK_SEQ(next_val) values(1);");

        // Reviving foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        jdbcTemplate.execute("update user_account set balance = 100 where id>0;");
        jdbcTemplate.execute("update user_account set effective_balance = 100 where id>0;");

        log.warn("Transaction tables and balances reset");
    }

    @Transactional
    public void dropDatabase() {

        // Removing foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

        // Dropping all transaction tables
        for (String table : tables) {
            jdbcTemplate.execute("DROP TABLE " + table);
        }

        // Reviving foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        jdbcTemplate.execute("update user_account set balance = 100 where id>0;");
        jdbcTemplate.execute("update user_account set effective_balance = 100 where id>0;");

        log.warn("Transaction tables dropped, balances reset");
        log.warn("Restart the server");
        exitService.exitApplication(0);

    }
}