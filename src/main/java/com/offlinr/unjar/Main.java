package com.offlinr.unjar;

import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "unjar"
        , version = "0.0.1"
        , description = "unpack the .jar into a directory"
)
public class Main implements Callable<Integer> {
    @CommandLine.Option(names = {"--into"}, required = true)
    private String into;

    @CommandLine.Parameters(index = "0", description = "target .jar file")
    private File file;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        final Path prefix = Files.createDirectories(Paths.get(into));

        JarExpert.unzip(prefix, file);

        return 0;
    }
}
