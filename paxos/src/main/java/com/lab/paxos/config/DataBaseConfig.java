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

    String createEventSql = """
            CREATE EVENT IF NOT EXISTS cleanup_duplicate_transaction_blocks
            ON SCHEDULE EVERY 3 SECOND
            DO
            BEGIN
                DECLARE v_count INT;
                DECLARE v_senderId BIGINT;
                DECLARE v_receiverId BIGINT;
                DECLARE v_amount BIGINT;
                DECLARE transaction_json MEDIUMTEXT;
                DECLARE transaction_count INT;
                DECLARE i INT DEFAULT 0;
                DECLARE v_hash VARCHAR(255);  -- Assuming a reasonable size for the hash
                DECLARE done INT DEFAULT FALSE;

                DECLARE cur CURSOR FOR 
                    SELECT hash FROM transaction_block GROUP BY hash HAVING COUNT(*) > 1;

                DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

                OPEN cur;

                read_loop: LOOP
                    FETCH cur INTO v_hash;
                    
                    IF done THEN
                        LEAVE read_loop;
                    END IF;

                    -- Get the count of duplicates for the current hash
                    SELECT COUNT(*) INTO v_count FROM transaction_block WHERE hash = v_hash;

                    IF v_count > 1 THEN
                        SET transaction_json = (SELECT transactions FROM transaction_block WHERE hash = v_hash LIMIT 1);
                        SET transaction_count = JSON_LENGTH(transaction_json);

                        -- Perform reverse transactions n-1 times
                        WHILE v_count > 1 DO
                            SET i = 0;
                            WHILE i < transaction_count DO
                                SET v_senderId = CAST(JSON_UNQUOTE(JSON_EXTRACT(transaction_json, CONCAT('$[', i, '].senderId'))) AS UNSIGNED);
                                SET v_receiverId = CAST(JSON_UNQUOTE(JSON_EXTRACT(transaction_json, CONCAT('$[', i, '].receiverId'))) AS UNSIGNED);
                                SET v_amount = CAST(JSON_UNQUOTE(JSON_EXTRACT(transaction_json, CONCAT('$[', i, '].amount'))) AS UNSIGNED);

                                -- Reverse the transaction
                                UPDATE user_account SET balance = balance + v_amount WHERE id = v_senderId;
                                UPDATE user_account SET balance = balance - v_amount WHERE id = v_receiverId;

                                SET i = i + 1;
                            END WHILE;

                            -- Delete one of the duplicate transaction blocks
                            DELETE FROM transaction_block WHERE hash = v_hash LIMIT 1;
                            SET v_count = v_count - 1;
                        END WHILE;
                    END IF;
                END LOOP;

                CLOSE cur;
            END
            """;


    @PostConstruct
    public void init() {
        if(developerMode && socketService.getAssignedPort()>0){
            log.warn("Resetting balances & transactions");
            databaseResetService.resetDatabase();
//            databaseResetService.dropDatabase();

            jdbcTemplate.execute("DROP PROCEDURE IF EXISTS perform_transaction");
            jdbcTemplate.execute(CREATE_PROCEDURE_SQL);
            jdbcTemplate.execute("DROP EVENT IF EXISTS cleanup_duplicate_transaction_blocks");
            jdbcTemplate.execute(createEventSql);
        }
    }
}
