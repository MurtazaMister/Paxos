package com.lab.paxos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@NamedStoredProcedureQuery(
        name = "performTransaction",
        procedureName = "perform_transaction",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_senderId", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_receiverId", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_amount", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_updatedRows", type = Integer.class)
        }
)
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private Long balance;
}
