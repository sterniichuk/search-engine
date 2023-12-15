package service;

import domain.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The class is involved in the reduce phase to group all document IDs by word
 */
public class Inverter {

    /**
     * Groups all documents by word
     * @param segments part of the (term, docID) pairs
     * @return entries (term, [posting1, posting2, posting3...]), where term is a word that occurs in specific files with docID.
     * Posting contains docID.
     * @see Posting
     */
    public List<Entry> reduce(List<Segment> segments) {
        Map<String, List<DocId>> postingsByTerm = segments.stream()
                .flatMap(segment -> segment.pairs().stream())
                .collect(Collectors.groupingBy(TermDocIdPair::term,
                        Collectors.mapping(TermDocIdPair::docId, Collectors.toList())));
        return postingsByTerm.entrySet().stream()
                .map(entry -> new Entry(entry.getKey(), reduceToPosting(entry.getValue())))
                .toList();
    }

    private List<Posting> reduceToPosting(List<DocId> docIds) {
        Map<String, List<Short>> groupedByIds = docIds.stream()
                .collect(Collectors.groupingBy(
                        docId -> docId.folder() + "-" + docId.docId(),
                        Collectors.mapping(DocId::position, Collectors.toList())
                ));

        return groupedByIds.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    byte folder = Byte.parseByte(parts[0]);
                    int docId = Integer.parseInt(parts[1]);
                    return new Posting(folder, docId, entry.getValue());
                })
                .toList();
    }
}
