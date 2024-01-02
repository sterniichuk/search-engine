package controller;

import config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import service.ThreadPool;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Slf4j
@RequiredArgsConstructor
public class Server implements AutoCloseable {
    private final ServerSocket serverSocket;
    private volatile boolean isWorking = true;

    public void work() {
        try (var clientHandlerExecutor = ThreadPool.newFixedThreadPool(8)) {
            while (isWorking) {
                Socket accept = serverSocket.accept();
                if (!isWorking) {
                    accept.close();
                    log.info("Last waiting client socket died properly");
                    return;
                }
                clientHandlerExecutor.submit(() -> {
                    try (ClientHandler handler = new ClientHandler(accept, this::kill)) {
                        handler.run();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (SocketTimeoutException e) {
            log.error("Timeout");
        } catch (IOException e) {
            if (isWorking) {
                isWorking = false;
                throw new RuntimeException(e);
            }
        }
    }

    private void kill() {
        isWorking = false;
        boolean alive = true;
        while (alive) {
            //noinspection EmptyTryBlock
            try (@SuppressWarnings("unused") var socket = new Socket(Config.host, Config.serverPort)) {
                // Establishing a socket connection to unblock any threads waiting for new clients on the .accept() method.
            } catch (ConnectException e) {
                alive = false;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }
}
