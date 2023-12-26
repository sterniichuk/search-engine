package controller;

import config.Config;
import domain.Query;
import domain.Response;
import protocol.Request;
import protocol.RequestBuilder;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public void doQueries(List<Query> queries) {
        for (var query : queries) {
            try (Socket socket = new Socket(Config.host, Config.serverPort);
                 var out = new DataOutputStream(socket.getOutputStream());
                 var in = new DataInputStream(socket.getInputStream())) {
                out.writeUTF(Request.SEARCH.toString());
                var responseCode = in.readInt();
                if (responseCode != Request.OK) {
                    throw new RuntimeException("Not OK for searching");
                }
                out.writeUTF(query.text());
                int size = RequestBuilder.SIZE.getInt(in.readUTF());
                List<Response> responses = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    int id = in.readInt();
                    String folder = in.readUTF();
                    responses.add(new Response(folder, id));
                }
                if (!query.expected().equals(responses)) {
                    System.err.println(STR."""
                            expected:\{query.expected()}
                            actual:\{responses}
                            """);
                    throw new RuntimeException("Response doesn't match expected list");
                }
                out.writeInt(Request.OK);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}