package controller;

import domain.Response;
import lombok.RequiredArgsConstructor;
import protocol.Request;
import protocol.RequestBuilder;
import service.SearchService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class SearchController {
    private final SearchService service;
    private static SearchController controller;

    public static SearchController getInstance() {
        return controller;
    }

    public void search(BufferedReader reader, PrintWriter writer) {
        try {
            String query = reader.readLine();
            List<Response> response = service.search(query);
            writer.println(RequestBuilder.SIZE.putValue(response.size()));
            for (var r : response) {
                writer.write(r.id());
                writer.write(r.path());
            }
            int read = reader.read();
            if (read != Request.OK) {
                System.err.println(STR. "Not OK for \{ query }" );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void setInstance(Supplier<SearchController> supplier) {
        if (controller == null) {
            System.out.println("Building");
            controller = supplier.get();
            System.out.println("finished");
        }
    }
}