package domain;

import java.util.List;

/**
 * Contains term and list of postings. Created in reduce phase of indexing
 */
public record Entry(String term, List<Posting> termDocIds) {
}
