package io.github.naminhyeok.core.support.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CsvParser {

    public Stream<CsvRow> parse(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        return reader.lines()
            .skip(1)
            .filter(line -> !line.trim().isEmpty())
            .map(this::parseLine);
    }

    private CsvRow parseLine(String line) {
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
