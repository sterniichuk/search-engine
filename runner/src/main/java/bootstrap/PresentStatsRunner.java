package bootstrap;

import java.time.LocalDateTime;

import static bootstrap.Bootstrap.formatter;
import static bootstrap.Bootstrap.presentStats;

public class PresentStatsRunner {
    public static void main(String[] args) {
        presentStats(LocalDateTime.now().format(formatter));
    }
}