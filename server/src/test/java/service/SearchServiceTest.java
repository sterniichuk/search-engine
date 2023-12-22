package service;

import domain.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static config.Config.DEFAULT_PATHS;
import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {

    @Test
    void findThreeFiles() {
        //given
        MasterNode master = new MasterNode();
        var response = master.buildIndexFromSource(List.of(DEFAULT_PATHS.getFirst()), 1, 8);
        SearchService s = new SearchService(response.index(), response.numberToFolder(), 1);
        var phraseFromFiles = "'Officer and a Gentleman.'";//from datasets/aclImdb/test/neg/1_3.txt,2_3.txt, 3_4.txt
        var files = List.of(1, 2, 3);
        List<Response> expectedResponse = files.stream().map(i -> new Response("..\\datasets\\aclImdb\\test\\neg", i)).toList();
        //when
        List<Response> searchResult = s.search(phraseFromFiles);
        //then
        System.out.println(searchResult);
        assertEquals(expectedResponse, searchResult);
    }
}