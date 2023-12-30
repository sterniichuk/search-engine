package bootstrap;

import java.time.LocalDateTime;

import static bootstrap.Bootstrap.presentStats;
import static config.Config.formatter;

public class PresentStatsRunner {
    public static void main(String[] args) {
        presentStats(LocalDateTime.now().format(formatter));
    }
}