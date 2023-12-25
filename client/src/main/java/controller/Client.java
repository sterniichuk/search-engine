package controller;

import config.Config;
import domain.Query;
import domain.Response;
import protocol.Request;
import protocol.RequestBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public void doQueries(List<Query> queries) {
        try (Socket socket = new Socket(Config.host, Config.serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            for (var query : queries) {
                out.println(Request.SEARCH);
                var responseCode = in.read();
                if (responseCode != Request.OK) {
                    throw new RuntimeException("Not OK for searching");
                }
                out.println(query.text());
                int size = RequestBuilder.SIZE.getInt(in.readLine());
                List<Response> responses = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    int id = in.read();
                    String folder = in.readLine();
                    responses.add(new Response(folder, id));
                }
                if (!query.expected().equals(responses)) {
                    throw new RuntimeException("Response doesn't match expected list");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}