package service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class ThreadPoolTest {

    record Task(int id, AtomicInteger counter, int increment, int times) implements Runnable {

        @Override
        public void run() {
            log.info(STR. "Started. id: \{ id }" );
            for (int i = 0; i < times; i++) {
                counter.addAndGet(increment);
                try {
                    Thread.sleep(increment * 3L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info(STR. "Finished. id: \{ id }" );
        }
    }

    @Test
    void submit() {
        AtomicInteger counter = new AtomicInteger();
        var rand = new Random();
        var list = IntStream.range(0, 25)
                .mapToObj(i -> new Task(i, counter, rand.nextInt(50) + 1, rand.nextInt(2) + 1))
                .toList();
        var expectedSum = list.stream().mapToInt(t -> t.times * t.increment).sum();
        try (ThreadPool pool = new ThreadPool(3)) {
            list.forEach(pool::submit);
        }
        assertEquals(expectedSum, counter.get());
    }
}