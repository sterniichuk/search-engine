package service;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface NodeExecutorService {
    <T> Future<T> assignTask(Callable<T> callable);
}
