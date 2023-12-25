package controller;

import lombok.RequiredArgsConstructor;
import protocol.Request;

import java.io.*;
import java.net.Socket;

@RequiredArgsConstructor
public class ClientHandler implements AutoCloseable {
    private final Socket clientSocket;
    private final Runnable kill;

    public void run() {
        try (var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             var out = new PrintWriter(clientSocket.getOutputStream(), true);
             OutputStream outS = clientSocket.getOutputStream()) {
            System.out.println("Accepted socket");
            Request request = Request.valueOf(in.readLine());
            switch (request) {
                case BUILD -> (new BuilderController()).handleBuilding(in, out, outS);
                case SEARCH -> SearchController.getInstance().search(in, out);
                case KILL -> {
                    out.println(Request.OK);
                    kill.run();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        clientSocket.close();
    }
}
