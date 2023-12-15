package domain;

public record TermDocId(String term, DocId docId) implements Comparable<TermDocId> {
    @Override
    public int compareTo(TermDocId o) {
        int termSort = this.term.compareTo(o.term);
        if (termSort != 0) {
            return termSort;
        }
        return this.docId.compareTo(o.docId);
    }
}