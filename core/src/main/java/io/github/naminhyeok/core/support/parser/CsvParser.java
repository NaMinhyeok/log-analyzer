package io.github.naminhyeok.core.support.parser;

import io.github.naminhyeok.core.support.error.CoreException;
import io.github.naminhyeok.core.support.error.ErrorType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class CsvParser {

    private CsvParser() {
    }

    public static void parse(InputStream inputStream, Consumer<Stream<CsvRow>> consumer) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            Stream<CsvRow> stream = reader.lines()
                .skip(1)
                .filter(line -> !line.trim().isEmpty())
                .map(CsvParser::parseLine);

            consumer.accept(stream);
        } catch (Exception e) {
            throw new CoreException(ErrorType.FILE_READ_ERROR);
        }
    }

    private static CsvRow parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim());

        return new CsvRow(fields.toArray(new String[0]));
    }
}
