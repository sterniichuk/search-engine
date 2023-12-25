package domain;

import java.util.List;

public record Query(String text, List<Response> expected) {
}
