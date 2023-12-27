package bootstrap;

import config.Config;
import controller.Server;

import java.net.ServerSocket;

public class ServerRunner {
    public static void main(String[] args) {
        int port = Config.serverPort;
        try (var server = new Server(new ServerSocket(port))) {
            System.out.println("Server started on port " + port);
            server.work();
            System.out.println("Server has finished the work");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}