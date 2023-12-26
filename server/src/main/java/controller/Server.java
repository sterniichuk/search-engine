package controller;

import config.Config;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.ConnectException;
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
                if (!isWorking) {
                    accept.close();
                    System.out.println("Last waiting client socket died properly");
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
        boolean alive = true;
        while (alive) {
            //noinspection EmptyTryBlock
            try (@SuppressWarnings("unused") var socket = new Socket(Config.host, Config.serverPort)) {
                // Establishing a socket connection to unblock any threads waiting for new clients on the .accept() method.
            } catch (ConnectException e) {
                alive = false;
                System.out.println("Killed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }
}
