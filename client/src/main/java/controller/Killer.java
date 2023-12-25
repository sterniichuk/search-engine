package controller;

import config.Config;
import protocol.Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Killer {
    public int askServerToFinish() {
        try (Socket socket = new Socket(Config.host, Config.serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(Request.KILL);
            var responseCode = in.read();
            if (responseCode != Request.OK) {
                System.out.println("Not OK for kill");
                return responseCode;
            }
            return responseCode;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
