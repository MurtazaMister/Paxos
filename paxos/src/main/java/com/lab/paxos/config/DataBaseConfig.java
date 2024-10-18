package com.lab.paxos.config;

import com.lab.paxos.service.DatabaseResetService;
import com.lab.paxos.service.SocketService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Slf4j
@DependsOn("dataSource")
public class DataBaseConfig {

    @Autowired
    SocketService socketService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseResetService databaseResetService;

    @Value("${app.developer-mode}")
    private boolean developerMode;

    private static final String CREATE_PROCEDURE_SQL =
            "CREATE PROCEDURE perform_transaction(" +
                    "    IN p_senderId INT, " +
                    "    IN p_receiverId INT, " +
                    "    IN p_amount INT, " +
                    "    OUT p_updatedRows INT " +
                    ") " +
                    "BEGIN " +
                    "    DECLARE sender_balance INT; " +
                    "    SELECT balance INTO sender_balance " +
                    "    FROM user_account " +
                    "    WHERE id = p_senderId; " +
                    "    IF sender_balance >= p_amount THEN " +
                    "        UPDATE user_account ua " +
                    "        SET balance = CASE " +
                    "            WHEN ua.id = p_senderId THEN ua.balance - p_amount " +
                    "            WHEN ua.id = p_receiverId THEN ua.balance + p_amount " +
                    "        END " +
                    "        WHERE ua.id IN (p_senderId, p_receiverId); " +
                    "        SET p_updatedRows = 2; " +
                    "    ELSE " +
                    "        SET p_updatedRows = 0; " +
                    "    END IF; " +
                    "END;";


    @PostConstruct
    public void init() {
        if(developerMode && socketService.getAssignedPort()>0){
            log.warn("Resetting balances & transactions");
            databaseResetService.resetDatabase();
//            databaseResetService.dropDatabase();

            jdbcTemplate.execute("DROP PROCEDURE IF EXISTS perform_transaction");
            jdbcTemplate.execute(CREATE_PROCEDURE_SQL);
        }
    }
}
