package service;

import domain.Posting;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PostingUpdaterTest {

    @Test
    void mergeSortedLists() {
        List<Posting> recipientSource = List.of(
                new Posting((byte) 1, 1, null),
                new Posting((byte) 1, 4, null),
                new Posting((byte) 2, 2, null),
                new Posting((byte) 2, 5, null)
        );
        List<Posting> recipient = new ArrayList<>(recipientSource);

        List<Posting> donor = List.of(
                new Posting((byte) 1, 2, null),
                new Posting((byte) 1, 3, null),
                new Posting((byte) 2, 3, null),
                new Posting((byte) 2, 4, null),
                new Posting((byte) 2, 6, null),
                new Posting((byte) 3, 6, null)
        );
        var updater = new PostingUpdater();
        updater.mergeSortedLists(recipient, donor);
        assertEquals(recipient.size(), recipientSource.size() + donor.size());
        assertEquals(recipient, Stream.concat(recipientSource.stream(), donor.stream()).sorted().toList());
    }
}