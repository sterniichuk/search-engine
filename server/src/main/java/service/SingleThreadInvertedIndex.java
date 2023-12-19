package service;

import domain.Entry;
import domain.Posting;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class SingleThreadInvertedIndex implements InvertedIndex {
    private final Map<String, List<Posting>> map;

    public SingleThreadInvertedIndex(int initialCapacity) {
        this(new HashMap<>(initialCapacity));
    }

    @Override
    public List<Posting> get(String term) {
        List<Posting> res = map.get(term);
        return (res != null) ? res : List.of();
    }

    @Override
    public void put(String term, List<Posting> postings) {
        map.put(term, postings);//assume that term inserted once and only once because building is done in a single thread
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void forEach(BiConsumer<String, List<Posting>> action) {
        map.forEach(action);
    }

    @Override
    public List<Entry> toList() {
        return map.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList();
    }
}