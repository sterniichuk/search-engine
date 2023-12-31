package service;

import domain.Split;
import domain.TermDocIdPair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {
    private final String datasetPath = System.getProperty("indexdataset");

    @Test
    void map() {
        Parser parser = new Parser(new TextProcessor());
        String folder = datasetPath + "\\test\\neg";
        List<TermDocIdPair> map = parser.map(List.of(new Split(0, 2, folder)),
                Map.of(folder, 0));
        assertTrue(map.size() > 50 && map.size() < 350);
    }
}