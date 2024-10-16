package com.lab.paxos.util;

import com.lab.paxos.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ParseUtil {
    public Transaction parseTransaction(String cellValue) {
        Pattern pattern = Pattern.compile("\\(S(\\d+), S(\\d+), (\\d+)\\)");
        Matcher matcher = pattern.matcher(cellValue);

        if (matcher.matches()) {
            Long senderId = Long.parseLong(matcher.group(1));
            Long receiverId = Long.parseLong(matcher.group(2));
            Long amount = Long.parseLong(matcher.group(3));

            return Transaction.builder()
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .amount(amount)
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid transaction format: " + cellValue);
        }
    }

    public List<Integer> parseActiveServerList(String cellValue) {
        Pattern pattern = Pattern.compile("\\[S(\\d+)(?:, S(\\d+))*\\]");
        Matcher matcher = pattern.matcher(cellValue);

        List<Integer> ids = new ArrayList<>();
        if (matcher.find()) {
            for (String part : cellValue.replaceAll("[\\[\\]S]", "").split(", ")) {
                ids.add(Integer.parseInt(part));
            }
        } else {
            throw new IllegalArgumentException("Invalid list format: " + cellValue);
        }

        return ids;
    }
}
