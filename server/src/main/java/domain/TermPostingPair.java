package domain;

public record TermPostingPair(String term, Posting posting) implements Comparable<TermPostingPair> {
    @Override
    public int compareTo(TermPostingPair o) {
        int termSort = this.term.compareTo(o.term);
        if (termSort != 0) {
            return termSort;
        }
        return this.posting.compareTo(o.posting);
    }
}