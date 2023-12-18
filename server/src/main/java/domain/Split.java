package domain;

/**
 * Class is supposed to be used to split files into smaller parts to be parsed in parallel.
 *
 * @author Serhii
 */
public record Split(
        int start,
        int finish,
        String folder) {
    public int numberOfFiles() {
        return finish - start;
    }
}
