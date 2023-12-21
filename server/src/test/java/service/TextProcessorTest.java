package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextProcessorTest {

    @Test
    void testApostrophe() {
        //given
        var processor = new TextProcessor();
        var input = "Interesting fact, isn't it, that all dogs like meat? His dog's toy isn't broken, is it?";
        String[] expected = {"interest", "fact", "isnt", "it", "that", "all", "dog", "like", "meat",
                "his", "dog", "toy", "isnt", "broken", "is", "it"};
        //when
        var actual = processor.processText(input);
        //then
        assertArrayEquals(expected, actual);
    }
}