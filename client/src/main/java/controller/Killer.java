package controller;

import config.Config;
import protocol.Request;

import java.io.*;
import java.net.Socket;

public class Killer {
    public void askServerToFinish() {
        try (Socket socket = new Socket(Config.host, Config.serverPort);
             var out = new DataOutputStream(socket.getOutputStream());
             var in = new DataInputStream(socket.getInputStream())) {
            out.writeUTF(Request.KILL.toString());
            var responseCode = in.readInt();
            if (responseCode != Request.OK) {
                System.out.println("Not OK for kill: " + responseCode);
                return;
            }
            System.out.println("Killed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
