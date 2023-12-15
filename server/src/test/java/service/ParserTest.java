package service;

import domain.KeyValue;
import domain.Split;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void map() {
        Parser parser = new Parser(new TextProcessor());
        String start = new File("..\\datasets\\aclImdb\\test\\neg\\0_2.txt").getPath();
        File file = new File("..\\datasets\\aclImdb\\test\\neg\\2_3.txt");
        String finish = file.getPath();
        List<KeyValue> map = parser.map(List.of(new Split(start, finish)), Map.of(file.getParent(), 0));
        assertTrue(map.size() > 50 && map.size() < 350);
    }
}