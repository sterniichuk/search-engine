package service;

import domain.Posting;

import java.util.List;

public interface InvertedIndex {
    List<Posting> get(String term);

    void add(String term, List<Posting> postings);
}