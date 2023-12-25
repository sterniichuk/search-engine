package controller;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class Server implements AutoCloseable {
    private final ServerSocket serverSocket;
    private volatile boolean isWorking = true;

    public void work() {
        try (var clientHandlerExecutor = Executors.newFixedThreadPool(8)) {
            while (isWorking) {
                Socket accept = serverSocket.accept();
                clientHandlerExecutor.submit(() -> {
                    try (ClientHandler handler = new ClientHandler(accept, this::kill)) {
                        handler.run();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout");
        } catch (IOException e) {
            if (isWorking) {
                isWorking = false;
                throw new RuntimeException(e);
            }
        }
    }

    private void kill() {
        isWorking = false;
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }
}
