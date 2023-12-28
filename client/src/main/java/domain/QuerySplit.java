package domain;

import java.io.File;

public record QuerySplit(
        int start,
        int finish,
        File[] folder) {
    public int numberOfFiles() {
        return finish - start;
    }
}