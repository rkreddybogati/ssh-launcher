package com.scalr.ssh.fs;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class FileSystemManager {
    public static File getTemporaryFile (final String prefix, final String suffix) throws IOException {
        File tempFile = AccessController.doPrivileged(
                new PrivilegedAction<File>() {
                    @Override
                    public File run() {
                        try {
                            return File.createTempFile(prefix, suffix);
                        } catch (IOException e) {
                            return null;
                        }
                    }
                }
        );

        if (tempFile == null) {
            throw new IOException();
        }

        //TODO: Deletion!

        return tempFile;
    }

    public static String getUserHome () {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("user.home");
            }
        });
    }
}
