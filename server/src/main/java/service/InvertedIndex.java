package service;

import domain.Entry;
import domain.Posting;

import java.util.List;
import java.util.function.BiConsumer;

public interface InvertedIndex {
    List<Posting> get(String key);

    void put(String key, List<Posting> value);

    int size();

    void forEach(BiConsumer<String, List<Posting>> action);

    List<Entry> toList();
}