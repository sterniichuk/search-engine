package service;

import domain.Posting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostingUpdaterTest {

    @Test
    void mergeSortedLists() {
        List<Posting> recipient = List.of(
                new Posting((byte) 1, 1, null),
                new Posting((byte) 1, 4, null),
                new Posting((byte) 2, 2, null),
                new Posting((byte) 2, 5, null)
        );

        List<Posting> donor = List.of(
                new Posting((byte) 1, 2, null),
                new Posting((byte) 1, 3, null),
                new Posting((byte) 2, 3, null),
                new Posting((byte) 2, 4, null),
                new Posting((byte) 2, 6, null),
                new Posting((byte) 3, 6, null)
        );
        var updater = new PostingUpdater();
        List<Posting> actual = updater.mergeSortedLists(recipient, donor);
        assertEquals( recipient.size() + donor.size(), actual.size());
        assertEquals(Stream.concat(recipient.stream(), donor.stream()).sorted().toList(), actual);
    }
}