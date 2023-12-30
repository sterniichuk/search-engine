package bootstrap;

import config.Config;
import controller.Server;
import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;

@Slf4j
public class ServerRunner {
    public static void main(String[] args) {
        int port = Config.serverPort;
        try (var server = new Server(new ServerSocket(port))) {
            log.info("Server started on port " + port);
            server.work();
            log.info("Server has finished the work");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}