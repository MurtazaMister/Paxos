package com.lab.paxos.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab.paxos.model.Transaction;
import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class TransactionListConverterUtil implements AttributeConverter<List<Transaction>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(transactions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert transaction list to JSON", e);
        }
    }

    @Override
    public List<Transaction> convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();  // Return an empty list if the JSON is empty
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Transaction>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to transaction list", e);
        }
    }
}
