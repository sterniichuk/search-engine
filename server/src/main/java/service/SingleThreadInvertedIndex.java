package service;

import domain.Posting;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SingleThreadInvertedIndex implements InvertedIndex {
    private final Map<String, List<Posting>> map;

    public SingleThreadInvertedIndex(int initialCapacity) {
        this(new HashMap<>(initialCapacity));
    }

    @Override
    public List<Posting> get(String term) {
        List<Posting> postings = map.get(term);
        return postings != null ? postings : List.of();
    }

    @Override
    public void add(String term, List<Posting> postings) {
        map.put(term, postings);
    }
}