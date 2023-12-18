package service;

import domain.Entry;
import domain.Segment;
import domain.Split;
import domain.TermDocIdPair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InverterTest {

    @Test
    void reduce() {
        //given
        Parser parser = new Parser(new TextProcessor());
        String folder = "..\\datasets\\aclImdb\\test\\neg";
        List<TermDocIdPair> map = parser.map(List.of(new Split(0, 2, folder)),
                Map.of(folder, 0));
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