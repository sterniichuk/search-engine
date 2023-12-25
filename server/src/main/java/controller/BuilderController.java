package controller;

import protocol.RequestBuilder;
import protocol.Request;
import service.MasterNode;
import service.SearchService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BuilderController {
    public void handleBuilding(BufferedReader reader, PrintWriter writer, OutputStream outS) {
        try {
            outS.write(Request.OK);
            outS.flush();
            System.out.println("Send ok");
            int threads = RequestBuilder.THREADS.getInt(reader.readLine());
            System.out.println("get threads");
            int variant = RequestBuilder.VARIANT.getInt(reader.readLine());
            int numberOfFolders = RequestBuilder.FOLDERS.getInt(reader.readLine());
            List<String> folders = new ArrayList<>(numberOfFolders);
            for (int i = 0; i < numberOfFolders; i++) {
                var folder = RequestBuilder.FOLDER.getString(reader.readLine());
                folders.add(folder);
            }
            System.out.println("get folders");
            boolean valid = checkParameters(threads, variant, folders);
            int responseCode = valid ? Request.OK : Request.BAD_REQUEST;
            outS.write(responseCode);
            if (RequestBuilder.START.equalString(reader.readLine())) {
                Supplier<SearchController> supplier = () -> {
                    MasterNode node = new MasterNode();
                    System.out.println("before alo");
                    var masterResponse = node.buildIndexFromSource(folders, variant, threads);
                    System.out.println("alo");
                    var searcher = new SearchService(masterResponse.index(), masterResponse.numberToFolder());
                    return new SearchController(searcher);
                };
                System.out.println(STR. "Build index with parameters: threadNumber:\{ threads }; variant:\{ variant }; folders:\{ folders }" );
                SearchController.setInstance(supplier);
                System.out.println("Index constructed");
                outS.write(Request.CREATED);
                outS.flush();
                return;
            }
            outS.write(Request.BAD_REQUEST);
            outS.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkParameters(int threads, int variant, List<String> folders) {
        if (threads < 0 || variant < -1 || variant > 30) {
            return false;
        }
        return folders.stream().noneMatch(String::isBlank);
    }
}
