package controller;

import config.Config;
import domain.Query;
import domain.Response;
import lombok.extern.slf4j.Slf4j;
import protocol.Request;
import protocol.RequestBuilder;
import service.QueryFactory;

import java.io.*;
import java.net.BindException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Client {

    public enum ClientStatus {
        SUCCESS, BIND_EXCEPTION
    }

    public ClientStatus doQueries(int numberOfQueries, File[] files, boolean showBindException) {
        var rand = new Random();
        var factory = new QueryFactory(rand);
        List<Query> notFound = new ArrayList<>();
        int found = 0;
        try (Socket socket = new Socket(Config.host, Config.serverPort);
             var out = new DataOutputStream(socket.getOutputStream());
             var in = new DataInputStream(socket.getInputStream())) {
            out.writeUTF(Request.SEARCH.toString());
            for (int i = 0; i < numberOfQueries; i++) {
                Query query = factory.fromFile(files[rand.nextInt(files.length)]);
                List<Response> responses = sendRequest(out, in, query);
                if (!responses.contains(query.expected())) {
                    notFound.add(query);
                } else {
                    found++;
                }
                out.writeBoolean(!(i == numberOfQueries - 1));
            }

        } catch (BindException e) {
            if (showBindException) {
                log.error(e.getMessage());
            }
            return ClientStatus.BIND_EXCEPTION;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        showStats(notFound, found);
        return ClientStatus.SUCCESS;
    }

    private static List<Response> sendRequest(DataOutputStream out, DataInputStream in, Query query) throws IOException {
        var responseCode = in.readInt();
        if (responseCode != Request.OK) {
            throw new RuntimeException("Not OK for searching");
        }
        out.writeUTF(query.text());
        int size = RequestBuilder.SIZE.getInt(in.readUTF());
        List<Response> responses = new ArrayList<>(size);
        for (int j = 0; j < size; j++) {
            int id = in.readInt();
            String folder = in.readUTF();
            responses.add(new Response(folder, id));
        }
        out.writeInt(Request.OK);
        return responses;
    }

    private static final int allowedNotFoundPercent = 3;

    private static void showStats(List<Query> notFound, int found) {
        double all = found + notFound.size();
        int currentPercent = (int) ((notFound.size() / all) * 100);
        if (currentPercent > allowedNotFoundPercent) {
            log.info(STR. """
                Search statistic:
                Found:\t\{ found }\ttimes
                Not found:\t\{ notFound.size() }\ttimes
                Not found \{ currentPercent }%
                Not found queries: \{ notFound.size() < 5 ? notFound : notFound.subList(0, 5) }
                """ );
        }
    }
}