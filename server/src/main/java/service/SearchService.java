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
    private final int k;//two terms appear within k words of each other
    private final TextProcessor processor = new TextProcessor();

    public List<Response> search(String string) {
        String[] tokens = processor.processText(string);
        Map<String, List<Posting>> postings = getMap(tokens);
        if (postings.isEmpty()) {
            return List.of();
        }
        var l = IntStream.range(0, tokens.length - 1)
                .mapToObj(i -> positionalIntersect(postings.get(tokens[i]), postings.get(tokens[i + 1]), k))
                .toList();
        int prevSize = -1;
        while (prevSize <= l.size() && l.size() > 1) {
            prevSize = l.size();
            List<List<Posting>> finalL = l;
            l = IntStream.range(0, l.size() - 1)
                    .mapToObj(i -> positionalIntersect(finalL.get(i), finalL.get(i + 1), k))
                    .toList();

        }
        return l.stream()
                .flatMap(Collection::stream)
                .distinct()
                .map(p -> new Response(folderMapper.get((int)p.folder()), p.docId()))
                .toList();
    }

    private Map<String, List<Posting>> getMap(String[] tokens) {
        return Arrays.stream(tokens)
                .distinct()
                .collect(Collectors.toMap(s -> s, index::get));
    }


    /**
     * @link https://www.youtube.com/watch?v=QVVvx_Csd2I&list=PLaZQkZp6WhWwoDuD6pQCmgVyDbUWl_ZUi&t=406s
     * @link https://nlp.stanford.edu/IR-book/pdf/irbookonlinereading.pdf Page 42(or 79 in pdf)
     * Introduction to Information Retrieval. Cambridge University Press Cambridge, England
     */
    private List<Posting> positionalIntersect(List<Posting> posting1, List<Posting> posting2, int index) {
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
                    l.forEach(ps -> answer.add(new Posting(p1.t.folder(), p1.t.docId(), List.of(pp1.t, ps))));
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