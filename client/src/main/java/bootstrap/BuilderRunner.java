package bootstrap;

import config.Config;
import controller.Builder;
import protocol.Request;

public class BuilderRunner {
    public static void main(String[] args) {
        System.out.println("Java version: " + System.getProperty("java.version"));
        int threadNumber = 8;
        int variant = 1;
        var folders = Config.DEFAULT_PATHS.subList(0, 1).stream().map(s->"C:\\Users\\stern\\IdeaProjects\\search-engine\\" + s.replace("..\\", "")).toList();
        System.out.println(folders);
        int code = (new Builder()).buildIndex(threadNumber, variant, folders);
        if(code != Request.CREATED){
            System.err.println("Build index response: " + code);
        }
    }
}
