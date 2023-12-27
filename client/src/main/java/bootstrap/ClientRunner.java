package bootstrap;

import config.Config;
import controller.Client;
import domain.Query;
import domain.Response;

import java.util.List;

public class ClientRunner {
    public static void main(String[] args) {
        var folders = Config.DEFAULT_PATHS.subList(0, 1).stream().map(s->"C:\\Users\\stern\\IdeaProjects\\search-engine\\" + s.replace("..\\", "")).toList();
        var client = new Client();
        List<Response> expected = List.of(new Response(folders.getFirst(), 6));
        client.doQueries(List.of(new Query("British playwright Ronald Harwood adapts", expected)));
    }
}
