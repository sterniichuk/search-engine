package controller;

import protocol.RequestBuilder;
import protocol.Request;
import service.MasterNode;
import service.SearchService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BuilderController {
    public void handleBuilding(DataInputStream in, DataOutputStream out) {
        System.out.println("Building request");
        try {
            out.writeInt(Request.OK);
            int threads = RequestBuilder.THREADS.getInt(in.readUTF());
            int variant = RequestBuilder.VARIANT.getInt(in.readUTF());
            int numberOfFolders = RequestBuilder.FOLDERS.getInt(in.readUTF());
            List<String> folders = new ArrayList<>(numberOfFolders);
            for (int i = 0; i < numberOfFolders; i++) {
                var folder = RequestBuilder.FOLDER.getString(in.readUTF());
                folders.add(folder);
            }
            var timeStampFromClient = RequestBuilder.TIME_STAMP.getString(in.readUTF());
            boolean valid = checkParameters(threads, variant, folders);
            int responseCode = valid ? Request.OK : Request.BAD_REQUEST;
            out.writeInt(responseCode);
            if (RequestBuilder.START.equalString(in.readUTF())) {
                Supplier<SearchController> supplier = () -> {
                    MasterNode node = new MasterNode();
                    var masterResponse = node.buildIndexFromSource(folders, variant, threads, timeStampFromClient);
                    var searcher = new SearchService(masterResponse.index(), masterResponse.numberToFolder());
                    return new SearchController(searcher);
                };
                System.out.println(STR. "Build index with parameters: threadNumber:\{ threads }; variant:\{ variant }; folders:\{ folders }" );
                SearchController.setInstance(supplier);
                System.out.println("Index constructed");
                out.writeInt(Request.CREATED);
                return;
            }
            out.writeInt(Request.BAD_REQUEST);
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
