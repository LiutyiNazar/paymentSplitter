package com.eleks.userservice.serializer;

import com.eleks.userservice.exception.InvalidDateFormatException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;
import static java.time.LocalDate.parse;

public class CustomJsonDeserializer extends JsonDeserializer<LocalDate> {
    public static final String PATTERN = "dd-MM-yyyy";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);

    @Override
    public LocalDate deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext) throws IOException {
        try {
            return parse(jsonParser.getText(), formatter);
        } catch (Exception e) {
            String message = format("Incorrect format of %s. Valid pattern is %s", jsonParser.getCurrentName(), PATTERN);
            throw new InvalidDateFormatException(message);
        }
    }
}
