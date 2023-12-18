package service;

import domain.Posting;

import java.util.List;

public class PostingUpdater {

    public void mergeSortedLists(List<Posting> recipient, List<Posting> donor) {
        int recipientIndex = 0;
        int donorIndex = 0;
        while (donorIndex < donor.size()) {
            Posting donorPosting = donor.get(donorIndex);
            while (recipientIndex < recipient.size()) {
                Posting recipientPosting = recipient.get(recipientIndex);
                if (recipientPosting.compareTo(donorPosting) > 0) {
                    recipient.add(recipientIndex, donorPosting);
                    donorIndex++;
                    break;
                }
                recipientIndex++;
            }
            if (recipientIndex == recipient.size()) {
                recipient.addAll(donor.subList(donorIndex, donor.size()));
                break;
            }
        }
    }
}
