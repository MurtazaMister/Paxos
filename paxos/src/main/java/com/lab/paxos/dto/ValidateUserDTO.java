package com.lab.paxos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserDTO {
    @NotNull(message = "User id cannot be null")
    private long userId;
    @NotNull(message = "Password cannot be null")
    private String password;
}
