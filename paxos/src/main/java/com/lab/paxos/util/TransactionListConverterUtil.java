package com.lab.paxos.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.paxos.model.Transaction;
import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class TransactionListConverterUtil implements AttributeConverter<List<Transaction>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Transaction> transactions) {
        try {
            // Convert the list to JSON string before saving to the database
            return objectMapper.writeValueAsString(transactions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert transaction list to JSON", e);
        }
    }

    @Override
    public List<Transaction> convertToEntityAttribute(String json) {
        try {
            // Convert the JSON string back to a list when reading from the database
            return objectMapper.readValue(json, new TypeReference<List<Transaction>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to transaction list", e);
        }
    }
}
