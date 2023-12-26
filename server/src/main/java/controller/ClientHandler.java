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
        try (var in = new DataInputStream(clientSocket.getInputStream());
             var out = new DataOutputStream(clientSocket.getOutputStream())) {
            Request request = Request.valueOf(in.readUTF());
            System.out.println(request);
            switch (request) {
                case BUILD -> (new BuilderController()).handleBuilding(in, out);
                case SEARCH -> SearchController.getInstance().search(in, out);
                case KILL -> {
                    out.writeInt(Request.OK);
                    kill.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        clientSocket.close();
    }
}
