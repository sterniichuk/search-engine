package service;

import domain.Posting;
import domain.Response;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class SearchService {
    private final InvertedIndex index;
    private final Map<Integer, String> folderMapper;
    private final TextProcessor processor = new TextProcessor();


    public List<Response> search(String string) {
        String[] tokens = processor.processText(string);
        Map<String, List<Posting>> postings = getMap(tokens);
        if (postings.isEmpty() || postings.entrySet().stream().anyMatch(e -> e.getValue().isEmpty())) {
            return List.of();
        }
        var l = minimizeSearchResult(tokens, postings);
        return l.stream()
                .flatMap(Collection::stream)
                .distinct()
                .filter(p -> l.stream().filter(list -> !list.isEmpty()).allMatch(s -> s.contains(p)))
                .map(p -> new Response(folderMapper.get((int) p.folder()), p.docId()))
                .toList();
    }

    /**
     * Performs the initial minimization of the search results.
     * <p>
     * This method identifies the shortest list of search results based on the provided tokens
     * and intersects it with other lists using an appropriate offset 'k'.
     *
     * @param tokens   Processed words obtained from the search request.
     * @param postings A map containing token-to-postings associations, where the key is the token,
     *                 and the value is a list of postings associated with that token.
     * @return A minimized collection of posting lists after intersecting with the shortest list.
     */
    private List<List<Posting>> minimizeSearchResult(String[] tokens, Map<String, List<Posting>> postings) {
        int shortest = findShortestList(tokens, postings);
        var shortestList = postings.get(tokens[shortest]);
        return IntStream.range(0, tokens.length)
                .filter(i -> i != shortest)
                .mapToObj(i -> positionalIntersect(postings.get(tokens[i]), shortestList, Math.abs(i - shortest)))
                .toList();
    }

    private int findShortestList(String[] tokens, Map<String, List<Posting>> postings) {
        int min = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < tokens.length; i++) {
            int size = postings.get(tokens[i]).size();
            if (size < min && size != 0) {
                min = size;
                index = i;
            }
        }
        return index;
    }

    private Map<String, List<Posting>> getMap(String[] tokens) {
        return Arrays.stream(tokens)
                .distinct()
                .collect(Collectors.toMap(s -> s, index::get));
    }

    /**
     * @param k two terms appear within k words of each other
     * @link <a href="https://www.youtube.com/watch?v=QVVvx_Csd2I&list=PLaZQkZp6WhWwoDuD6pQCmgVyDbUWl_ZUi&t=406s">7 6 Phrase Queries and Positional Indexes 19 45</a>
     * @link <a href="https://nlp.stanford.edu/IR-book/pdf/irbookonlinereading.pdf">Introduction to Information Retrieval. Cambridge University Press Cambridge, England. Page 42(or 79 in pdf)</a>
     */
    private List<Posting> positionalIntersect(List<Posting> posting1, List<Posting> posting2, int k) {
        List<Posting> notEmpty = checkEmpty(posting1, posting2);
        if (notEmpty != null) {
            return notEmpty;
        }
        List<Posting> answer = new ArrayList<>();
        var p1 = new NIterator<>(posting1);
        var p2 = new NIterator<>(posting2);
        while (p1.t != null && p2.t != null) {
            var c = p1.compareTo(p2);
            if (c == 0) {
                List<Short> l = new LinkedList<>();
                var pp1 = new NIterator<>(p1.t.positions());
                var pp2 = new NIterator<>(p2.t.positions());
                while (pp1.t != null) {
                    while (pp2.t != null) {
                        if (Math.abs(pp1.t - pp2.t) <= k) {
                            l.add(pp2.t);
                        } else if (pp2.t > pp1.t) {
                            break;
                        }
                        pp2.next();
                    }
                    while (!l.isEmpty() && Math.abs(l.getFirst() - pp1.t) > k) {
                        l.removeFirst();
                    }
                    l.forEach(ps -> {
                        if (!Objects.equals(pp1.t, ps)) {
                            answer.add(new Posting(p1.t.folder(), p1.t.docId(), List.of(pp1.t, ps)));
                        }
                    });
                    pp1.next();
                }
                p1.next();
                p2.next();
            } else if (c < 0) {
                p1.next();
            } else {
                p2.next();
            }
        }
        return answer;
    }

    private List<Posting> checkEmpty(List<Posting> posting1, List<Posting> posting2) {
        if (posting2.isEmpty()) {
            return posting1;
        } else if (posting1.isEmpty()) {
            return posting2;
        }
        return null;
    }

    private static class NIterator<T extends Comparable<T>> implements Comparable<NIterator<T>> {
        private final List<T> list;
        private T t;
        private int i;

        private NIterator(List<T> list) {
            this.list = list;
            t = list.isEmpty() ? null : list.getFirst();
        }

        public void next() {
            if (i < list.size() - 1) {
                t = list.get(++i);
                return;
            }
            t = null;
        }

        @Override
        public int compareTo(NIterator<T> o) {
            return t.compareTo(o.t);
        }
    }
}