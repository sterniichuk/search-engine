package domain;

/**
 * Class is supposed to be used to split files into smaller parts to be parsed in parallel.
 * @author Serhii
 * @param start first file that Parser will start from
 * @param finish last file that Parser will finish at
 */
public record Split(String start, String finish) {
}
