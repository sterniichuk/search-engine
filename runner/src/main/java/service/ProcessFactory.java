package service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ProcessFactory {
    public Process exec(Class<?> klass, List<String> args) throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getName();

        List<String> command = new LinkedList<>();
        command.add(javaBin);
        command.add("--enable-preview");
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        if (args != null) {
            command.addAll(args);
        }
        var builder = new ProcessBuilder(command);
        return builder.inheritIO().start();
    }
}
