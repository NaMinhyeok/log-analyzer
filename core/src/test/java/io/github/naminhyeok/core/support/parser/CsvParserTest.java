package io.github.naminhyeok.core.support.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.tuple;

class CsvParserTest {

    private CsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvParser();
    }

    @Test
    void CSV를_파싱할_수_있다() {
        // given
        String csv = """
            header1,header2,header3
            value1,value2,value3
            """;
        InputStream inputStream = toInputStream(csv);

        // when
        List<CsvRow> rows = parser.parse(inputStream).toList();

        // then
        then(rows).hasSize(1)
            .extracting(row -> row.get(0), row -> row.get(1), row -> row.get(2))
            .containsExactly(tuple("value1", "value2", "value3"));
    }

    @Test
    void 헤더_라인은_스킵된다() {
        // given
        String csv = """
            header1,header2,header3
            """;
        InputStream inputStream = toInputStream(csv);

        // when
        List<CsvRow> rows = parser.parse(inputStream).toList();

        // then
        then(rows).isEmpty();
    }

    @Test
    void 따옴표_안의_쉼표를_올바르게_처리한다() {
        // given
        String csv = """
            header1,header2
            "hello, world",value2
            """;
        InputStream inputStream = toInputStream(csv);

        // when
        List<CsvRow> rows = parser.parse(inputStream).toList();

        // then
        then(rows).hasSize(1)
            .extracting(row -> row.get(0), row -> row.get(1))
            .containsExactly(tuple("hello, world", "value2"));
    }

    @Test
    void 빈_줄은_스킵된다() {
        // given
        String csv = """
            header1,header2
            
            value1,value2
            
            """;
        InputStream inputStream = toInputStream(csv);

        // when
        List<CsvRow> rows = parser.parse(inputStream).toList();

        // then
        then(rows).hasSize(1);
    }

    @Test
    void 여러_줄을_파싱할_수_있다() {
        // given
        String csv = """
            header1,header2
            row1col1,row1col2
            row2col1,row2col2
            row3col1,row3col2
            """;
        InputStream inputStream = toInputStream(csv);

        // when
        List<CsvRow> rows = parser.parse(inputStream).toList();

        // then
        then(rows).hasSize(3)
            .extracting(row -> row.get(0), row -> row.get(1))
            .containsExactly(
                tuple("row1col1", "row1col2"),
                tuple("row2col1", "row2col2"),
                tuple("row3col1", "row3col2")
            );
    }

    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
