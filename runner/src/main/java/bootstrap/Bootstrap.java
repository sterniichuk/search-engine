package bootstrap;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import service.ProcessFactory;
public class Bootstrap {
    public static void main(String[] args) throws IOException, InterruptedException {
        var builder = new ProcessFactory();
        builder.exec(ServerRunner.class, List.of());//run server
        Thread.sleep(100);
        builder.exec(BuilderRunner.class, List.of()).waitFor();//send request to build index
        List<Process> list = IntStream.range(0, 10)
                .mapToObj(i -> {
                    try {
                        return builder.exec(ClientRunner.class, List.of());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();//send search requests
        for (Process process : list) {
            process.waitFor();//wait for all processes
        }
        Process killer = builder.exec(KillerRunner.class, List.of());
        killer.waitFor();//kill server and wait
    }
}
