package com.lab.paxos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    @NotNull
    private String unameSender;
    @NotNull
    private String unameReceiver;
    @NotNull
    private LocalDateTime timestamp;
    @Min(value = 1, message = "Amount should be greater than 0")
    private Long amount;
}
