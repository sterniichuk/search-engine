package controller;

import domain.Response;
import lombok.RequiredArgsConstructor;
import protocol.Request;
import protocol.RequestBuilder;
import service.SearchService;

import java.io.*;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class SearchController {
    private final SearchService service;
    private static SearchController controller;

    public static SearchController getInstance() {
        return controller;
    }

    public void search(DataInputStream in, DataOutputStream out) {
        try {
            out.writeInt(Request.OK);
            String query = in.readUTF();
            List<Response> response = service.search(query);
            out.writeUTF(RequestBuilder.SIZE.putValue(response.size()));
            for (var r : response) {
                out.writeInt(r.id());
                out.writeUTF(r.path());
            }
            int read = in.readInt();
            if (read != Request.OK) {
                System.err.println(STR. "Not OK for \{ query }" );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void setInstance(Supplier<SearchController> supplier) {
        if (controller == null) {
            controller = supplier.get();
        }
    }
}