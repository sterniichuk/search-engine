package domain;

public record TermDocIdPair(String term, DocId docId) implements Comparable<TermDocIdPair> {
    @Override
    public int compareTo(TermDocIdPair o) {
        int termSort = this.term.compareTo(o.term);
        if (termSort != 0) {
            return termSort;
        }
        return this.docId.compareTo(o.docId);
    }
}