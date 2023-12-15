package service;

import domain.Posting;
import org.junit.jupiter.api.Test;

import java.util.List;

import static config.Config.DEFAULT_PATHS;
import static org.junit.jupiter.api.Assertions.*;

class MasterNodeTest {

    @Test
    void buildIndexFromSource() {
        MasterNode master = new MasterNode();
        InvertedIndex index = master.buildIndexFromSource(List.of(DEFAULT_PATHS.getFirst()), 1, 1);
        String surnamePresentInTheFirstFile = "Costner";
        surnamePresentInTheFirstFile = (new TextProcessor().processText(surnamePresentInTheFirstFile))[0]; //adjust word to token
        List<Posting> postings = index.get(surnamePresentInTheFirstFile);
        assertFalse(postings.isEmpty());
        assertTrue(postings.stream().anyMatch(p -> p.docId() == 0 && p.folder() == 0 && p.positions().getFirst() == 3));
    }
}