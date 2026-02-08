package com.expensetracker.config;  // or your config package

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.YearMonth;

public class YearMonthFlexibleDeserializer extends JsonDeserializer<YearMonth> {
    @Override
    public YearMonth deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);


        if (node.isTextual()) {
            return YearMonth.parse(node.asText());
        }


        if (node.isObject()) {
            int year = node.get("year").asInt();
            int monthValue = node.get("monthValue").asInt();
            return YearMonth.of(year, monthValue);
        }

        throw new IllegalArgumentException("Invalid month format");
    }
}
