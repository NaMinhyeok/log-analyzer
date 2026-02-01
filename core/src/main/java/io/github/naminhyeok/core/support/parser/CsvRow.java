package io.github.naminhyeok.core.support.parser;

public record CsvRow(String[] fields) {

    public String get(int index) {
        if (index < 0 || index >= fields.length) {
            return null;
        }
        return fields[index];
    }

    public int size() {
        return fields.length;
    }
}
