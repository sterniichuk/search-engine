package service;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
public class ThreadPool implements AutoCloseable {
    private final Queue<Runnable> queue;
    private boolean isTerminated;
    private boolean initiatedTermination;
    private final List<Thread> workers;

    public ThreadPool(int numberOfWorkers) {
        workers = new ArrayList<>(numberOfWorkers);
        for (int i = 0; i < numberOfWorkers; ++i) {
            workers.add(new Thread(this::routine));
        }
        queue = new LinkedList<>();
        workers.forEach(Thread::start);
    }

    public static ThreadPool newFixedThreadPool(int threadNumber) {
        return new ThreadPool(threadNumber);
    }

    public synchronized void submit(Runnable runnable) {
        if (!isTerminated) {
            queue.add(runnable);
            if (queue.size() == 1) {
                this.notifyAll();//notify workers that they have some job to do
            }
        }
    }

    private void routine() {
        while (!isTerminated) {
            try {
                Runnable runnable = getTask();
                if (runnable != null) {
                    runnable.run();
                }
            } catch (InterruptedException e) {
                log.error(e.toString());
            }
        }
    }

    private synchronized Runnable getTask() throws InterruptedException {
        while (!isTerminated && !initiatedTermination && queue.isEmpty()) {
            this.wait();
        }
        Runnable poll = queue.poll();
        if (!isTerminated //if this flag is set to true there is no need to notify shutdown thread again
                && queue.isEmpty() && initiatedTermination) {
            this.notifyAll();//notify shutdown thread
        }
        return poll;
    }

    @Override
    public void close() {
        log.info("Started pool closing");
        try {
            shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (var worker : workers) {
            try {
                worker.join();//waiting for everyone to finish tasks
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Successfully closed the thread pool");
    }

    private synchronized void shutdown() throws InterruptedException {
        initiatedTermination = true;
        while (!queue.isEmpty()) {
            this.wait();//wait for a time when all tasks started executing
        }
        isTerminated = true;
        this.notifyAll();//wake up the workers. Work day is finished for them
    }
}
