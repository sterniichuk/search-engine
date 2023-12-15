package domain;

import java.util.List;

/**
 * Result of the reduce phase
 */
public record Posting(byte folder, int docId, List<Short> positions) {
}