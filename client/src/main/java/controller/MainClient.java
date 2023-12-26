package controller;

import config.Config;
import protocol.Request;
import protocol.RequestBuilder;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class MainClient {

    public int buildIndex(int threadNumber, int variant, List<String> folders) {

        try (Socket socket = new Socket(Config.host, Config.serverPort);
             var out = new DataOutputStream(socket.getOutputStream());
             var in = new DataInputStream(socket.getInputStream())) {
            out.writeUTF(Request.BUILD.toString());
            var responseCode = in.readInt();
            if (responseCode != Request.OK) {
                System.out.println("Not OK for building index: " + responseCode);
                return responseCode;
            }
            System.out.println("OK for building index");
            out.writeUTF(RequestBuilder.THREADS.putValue(threadNumber));
            out.writeUTF(RequestBuilder.VARIANT.putValue(variant));
            out.writeUTF(RequestBuilder.FOLDERS.putValue(folders.size()));
            for (var folder : folders) {
                out.writeUTF(RequestBuilder.FOLDER.putValue(folder));
            }
            responseCode = in.readInt();
            if (responseCode != Request.OK) {
                System.out.println("Not OK for parameters");
                return responseCode;
            }
            out.writeUTF(RequestBuilder.START.toString());
            responseCode = in.readInt();
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