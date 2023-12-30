package service;

import domain.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.Stream;

import static config.Config.DEFAULT_PATHS;
import static org.junit.jupiter.api.Assertions.*;
import static service.TestUtils.getPathToDataset;
import static service.TestUtils.toAbsolute;

class SearchServiceTest {

    @Test
    void findThreeFiles() {
        //given
        MasterNode master = new MasterNode();
        var response = master.buildIndexFromSource(toAbsolute(DEFAULT_PATHS.getFirst()), 1, 8);
        SearchService s = new SearchService(response.index(), response.numberToFolder(), 1);
        var phraseFromFiles = "'Officer and a Gentleman.'";//from datasets/aclImdb/test/neg/1_3.txt,2_3.txt, 3_4.txt
        List<Response> expectedResponse = Stream.of(1, 2, 3).map(i -> new Response(getPathToDataset() + "\\datasets\\aclImdb\\test\\neg", i)).toList();
        //when
        List<Response> searchResult = s.search(phraseFromFiles);
        //then
        System.out.println(searchResult);
        assertEquals(expectedResponse, searchResult);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            There is NO chemistry between Kutcher   |   1
            British playwright Ronald Harwood adapts|   6
            "Cover Girl" is a lacklustre WWII       |  27
            (get it??? - Jack L and Hyde - Jekyll   |  33
            """)
    void findOneFile(String phraseFromFiles, int id) {
        //given
        MasterNode master = new MasterNode();
        var response = master.buildIndexFromSource(toAbsolute(DEFAULT_PATHS.getFirst()), 1, 8);
        SearchService s = new SearchService(response.index(), response.numberToFolder(), 1);
        List<Response> expectedResponse = List.of(new Response(getPathToDataset() + "\\datasets\\aclImdb\\test\\neg", id));
        //when
        List<Response> searchResult = s.search(phraseFromFiles);
        //then
        assertEquals(expectedResponse, searchResult);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            Sterniichuk Serhii
            Kutcher likes Physics
            Kutcher and Serhii hate chemistry
            """)
    void findNoFile(String phraseFromFiles) {
        //given
        MasterNode master = new MasterNode();
        var response = master.buildIndexFromSource(toAbsolute(DEFAULT_PATHS.getFirst()), 1, 8);
        SearchService s = new SearchService(response.index(), response.numberToFolder(), 1);
        //when
        List<Response> searchResult = s.search(phraseFromFiles);
        //then
        assertEquals(List.of(), searchResult);
    }
}