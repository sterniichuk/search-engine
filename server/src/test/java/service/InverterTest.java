package service;

import domain.Entry;
import domain.Segment;
import domain.Split;
import domain.TermDocIdPair;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InverterTest {

    @Test
    void reduce() {
        //given
        Parser parser = new Parser(new TextProcessor());
        String start = new File("..\\datasets\\aclImdb\\test\\neg\\0_2.txt").getPath();
        File file = new File("..\\datasets\\aclImdb\\test\\neg\\2_3.txt");
        String finish = file.getPath();
        List<TermDocIdPair> map = parser.map(List.of(new Split(start, finish)), Map.of(file.getParent(), 0));
        //when
        Inverter inverter = new Inverter();
        List<Entry> reduce = inverter.reduce(List.of(new Segment(map)));
        //then
        assertTrue(reduce.size() < map.size());
        long count = reduce.stream()
                .flatMap(x -> x.postings().stream())
                .flatMap(x -> x.positions().stream())
                .count();
        assertEquals(map.size(), count);
    }
}