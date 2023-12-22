package service;

import domain.Posting;

import java.util.ArrayList;
import java.util.List;

public class PostingUpdater {

    public List<Posting> mergeSortedLists(List<Posting> l1, List<Posting> l2) {
        List<Posting> mergedList = new ArrayList<>(l1.size() + l2.size());
        int i1 = 0;
        int i2 = 0;
        while (i1 < l1.size() && i2 < l2.size()) {
            Posting p1 = l1.get(i1);
            Posting p2 = l2.get(i2);
            if (p1.compareTo(p2) < 0) {
                mergedList.add(p1);
                i1++;
            } else {
                mergedList.add(p2);
                i2++;
            }
        }
        fillLast(l1, i1, mergedList);
        fillLast(l2, i2, mergedList);
        return mergedList;
    }

    private static void fillLast(List<Posting> l1, int i1, List<Posting> mergedList) {
        while (i1 < l1.size()) {
            mergedList.add(l1.get(i1));
            i1++;
        }
    }
}
