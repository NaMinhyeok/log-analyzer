package io.github.naminhyeok.core.domain.counter;

import io.github.naminhyeok.core.domain.ParseError;

import java.util.ArrayList;
import java.util.List;

public class ParseErrorCollector {

    private static final int MAX_SAMPLES = 10;

    private final List<ParseError> samples;
    private int totalCount;

    public ParseErrorCollector() {
        this.samples = new ArrayList<>();
        this.totalCount = 0;
    }

    private ParseErrorCollector(List<ParseError> samples, int totalCount) {
        this.samples = new ArrayList<>(samples);
        this.totalCount = totalCount;
    }

    public void add(int lineNumber, String rawLine, String message) {
        totalCount++;
        if (samples.size() < MAX_SAMPLES) {
            samples.add(ParseError.of(lineNumber, rawLine, message));
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<ParseError> getSamples() {
        return List.copyOf(samples);
    }

    public ParseErrorCollector copy() {
        return new ParseErrorCollector(this.samples, this.totalCount);
    }
}
