package com.offlinr.unjar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarExpert {
    private static final String NULL_JAR_FILE =
            "Jar file must not be null";
    private static final String NULL_DESTINATION_PATH =
            "Destination path must not be null";
    private static final String NULL_JAR_INPUT_STREAM =
            "Input stream from jar file must not be null";
    private static final String NULL_JAR_INPUT_STREAM_CHARSET =
            "Charset of jar input stream must not be null";
    private static final String NULL_ZIP_INPUT_STREAM =
            "Input stream from zip file must not be null";

    public static void unzip(Path where, File jarFile) {
        if (where == null) {
            throw new IllegalArgumentException(NULL_DESTINATION_PATH);
        }
        if (jarFile == null) {
            throw new IllegalArgumentException(NULL_JAR_FILE);
        }

        try {
            unzip(where, new FileInputStream(jarFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unzip(Path where, FileInputStream fileInputStream) {
        if (where == null) {
            throw new IllegalArgumentException(NULL_DESTINATION_PATH);
        }
        if (fileInputStream == null) {
            throw new IllegalArgumentException(NULL_JAR_INPUT_STREAM);
        }

        unzip(where, fileInputStream, StandardCharsets.UTF_8);
    }

    public static void unzip(
            Path where, FileInputStream fileInputStream, Charset charset
    ) {
        if (where == null) {
            throw new IllegalArgumentException(NULL_DESTINATION_PATH);
        }
        if (fileInputStream == null) {
            throw new IllegalArgumentException(NULL_JAR_INPUT_STREAM);
        }
        if (charset == null) {
            throw new IllegalArgumentException(NULL_JAR_INPUT_STREAM_CHARSET);
        }

        unzip(where, new ZipInputStream(fileInputStream, charset));
    }

    public static void unzip(Path where, ZipInputStream zis) {
        if (where == null) {
            throw new IllegalArgumentException(NULL_DESTINATION_PATH);
        }
        if (zis == null) {
            throw new IllegalArgumentException(NULL_ZIP_INPUT_STREAM);
        }

        try {
            for (ZipEntry zipEntry = zis.getNextEntry();
                 zipEntry != null;
                 zipEntry = zis.getNextEntry()
            ) {
                final String name = zipEntry.getName();

                final Path target = makeTargetPath(where, name);

                if (zipEntry.isDirectory()) {
                    // If it is just a directory, create it and move on
                    try {
                        Files.createDirectories(target);
                    } catch (FileAlreadyExistsException e) {
                        // Some jars contain an entry for a file and for a
                        // directory with the same name. In this case, delete
                        // the file and create a directory instead.
                        // Example of a broken .jar:
                        // https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-api/3.141.59
                        System.err.printf("  The .jar file is malformed!%n");
                        System.err.printf(
                                "  Will convert 'file' %s into directory!%n", target);
                        Files.deleteIfExists(target);
                        Files.createDirectories(target);
                    }
                } else {
                    // If it is a non-directory entry, make sure the parent
                    // directory exists, and then copy the entry in there:
                    Files.createDirectories(target.getParent());
                    Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                }

                if (name.endsWith(".jar")) {
                    final String newName = name.substring(0, name.lastIndexOf("."));
                    final Path newPrefix = makeTargetPath(where, newName);

                    unzip(newPrefix,
                          new ZipInputStream(new FileInputStream(target.toFile()))
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path makeTargetPath(Path prefix, String name) {
        final Path path = prefix.resolve(name).normalize();

        if (!path.startsWith(prefix)) {
            throw new RuntimeException("ZipSlip vulnerability");
        }

        return path;
    }
}
