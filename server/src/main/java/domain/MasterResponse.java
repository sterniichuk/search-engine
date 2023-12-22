package domain;

import service.InvertedIndex;

import java.util.Map;

public record MasterResponse(InvertedIndex index, Map<Integer, String> numberToFolder) {
}