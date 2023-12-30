package controller;

import config.Config;
import lombok.extern.slf4j.Slf4j;
import protocol.Request;

import java.io.*;
import java.net.Socket;

@Slf4j
public class Killer {
    public void askServerToFinish() {
        try (Socket socket = new Socket(Config.host, Config.serverPort);
             var out = new DataOutputStream(socket.getOutputStream());
             var in = new DataInputStream(socket.getInputStream())) {
            out.writeUTF(Request.KILL.toString());
            var responseCode = in.readInt();
            if (responseCode != Request.OK) {
                log.info("Not OK for kill: " + responseCode);
                return;
            }
            log.info("Killed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
