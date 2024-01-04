package service;

import domain.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
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
        SearchService s = new SearchService(response.index(), response.numberToFolder());
        var phraseFromFiles = "'Officer and a Gentleman.'";//from datasets/aclImdb/test/neg/1_3.txt,2_3.txt, 3_4.txt
        List<Response> expectedResponse = Stream.of(1, 2, 3).map(i -> new Response(getPathToDataset() + "\\test\\neg", i)).toList();
        //when
        List<Response> searchResult = s.search(phraseFromFiles);
        //then
        System.out.println(searchResult);
        assertEquals(expectedResponse, searchResult);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            There is NO chemistry between Kutcher   |   1 | test\\neg
            British playwright Ronald Harwood adapts|   6 | test\\neg
            "Cover Girl" is a lacklustre WWII       |  27 | test\\neg
            (get it??? - Jack L and Hyde - Jekyll   |  33 | test\\neg
            not to waste their time or money        | 217 | test\\neg
            can make it as fun as possible          |  22 | test\\neg
            nothing in his eyes at all. It's like   | 121 | test\\neg
            Combs in one of his first roles!        |  53 | test\\neg
            for Laurel and Hardy fans, other people should stay away from it | 147 | test\\neg
            It's not that this movie is a           |  79 | test\\neg
            behind the heist and he plays Mr. Nice Guy at first, | 215 | test\\neg
            the mummy and the robot, and it's really hard to | 238 | test\\neg
            is nowhere near as annoying as he's presented in   | 230 | test\\neg
            people about this. With it's "you have  | 183 | test\\neg
            to think it is. I would have to         | 217 | test\\neg
            possess Nurse Sherri.<br /><br />Now, Sherri is obviously a | 39 | test\\neg
            thought it would be. As a heist film,   | 215 | test\\neg
            possess Nurse Sherri.<br /><br />Now, Sherri is obviously | 39 | test\\neg
            watch a film like this, you want to     | 237 | test\\neg
            added in and seem to have little        | 125 | test\\neg
            no, no, no, NO! This is not a film, this is | 79 | test\\neg
            I said, when will I ever learn?<br      | 108 | test\\neg
            you trying to figure out why they       | 248 | test\\neg
            her father, his mother and his boss (Orbach).<br /><br />It's pretty       | 574 | train\\unsup
            of her father, his mother and his boss (Orbach).<br /><br />It's pretty much an 80's | 574 | train\\unsup
            which ruffles the feathers of her father, his mother and his boss (Orbach).<br /><br | 574 | train\\unsup
            """)
    void findOneFile(String phraseFromFiles, int id, String folder) {
        //given
        MasterNode master = new MasterNode();
        var response = master.buildIndexFromSource(toAbsolute(folder), 1, 4);
        SearchService s = new SearchService(response.index(), response.numberToFolder());
        List<Response> expectedResponse = List.of(new Response(getPathToDataset() + File.separator + folder, id));
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
        SearchService s = new SearchService(response.index(), response.numberToFolder());
        //when
        List<Response> searchResult = s.search(phraseFromFiles);
        //then
        assertEquals(List.of(), searchResult);
    }
}