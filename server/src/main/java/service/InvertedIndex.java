package service;

import domain.DocumentIDs;

public interface InvertedIndex {
    DocumentIDs get(String string);
}
