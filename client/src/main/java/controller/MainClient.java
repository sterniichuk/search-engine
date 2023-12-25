package controller;

import config.Config;
import protocol.Request;
import protocol.RequestBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class MainClient {

    public int buildIndex(int threadNumber, int variant, List<String> folders) {

        try (Socket socket = new Socket(Config.host, Config.serverPort);
             InputStream inputStream = socket.getInputStream();
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            System.out.println("Build");
            out.println(Request.BUILD);
            System.out.println("after Build");
            var responseCode = inputStream.read();
            if (responseCode != Request.OK) {
                System.out.println("Not OK for building index: " + responseCode);
                return responseCode;
            }
            System.out.println("OK for building index");
            out.println(RequestBuilder.THREADS.putValue(threadNumber));
            out.println(RequestBuilder.VARIANT.putValue(variant));
            out.println(RequestBuilder.FOLDERS.putValue(folders.size()));
            for (var folder : folders) {
                out.println(RequestBuilder.FOLDER.putValue(folder));
            }
            System.out.println("Send parameters");
            responseCode = inputStream.read();
            if (responseCode != Request.OK) {
                System.out.println("Not OK for parameters");
                return responseCode;
            }
            out.println(RequestBuilder.START);
            responseCode = inputStream.read();
            if (responseCode != Request.CREATED) {
                System.out.println("Not created" + responseCode);
                return responseCode;
            }
            System.out.println("Got 201");
            return responseCode;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}