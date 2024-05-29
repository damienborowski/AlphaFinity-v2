package com.alphafinity.alphafinity.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String date = p.getText();

        // Try to parse with LocalDateTime formatters
        Optional<LocalDateTime> parsedDateTime = FORMATTERS.stream()
                .filter(formatter -> formatter.toString().contains("HH:mm:ss"))
                .map(formatter -> {
                    try {
                        return LocalDateTime.parse(date, formatter);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();

        if (parsedDateTime.isPresent()) {
            return parsedDateTime.get();
        }

        // Try to parse with LocalDate formatters and convert to LocalDateTime
        Optional<LocalDate> parsedDate = FORMATTERS.stream()
                .filter(formatter -> !formatter.toString().contains("HH:mm:ss"))
                .map(formatter -> {
                    try {
                        return LocalDate.parse(date, formatter);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();

        if (parsedDate.isPresent()) {
            return parsedDate.get().atStartOfDay();
        }

        throw new IOException("Unable to parse date: " + date);
    }
}
