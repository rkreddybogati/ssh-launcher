package com.scalr.ssh.fs;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class FileSystemManager {

    private static class FileExistencePrivilegedAction implements PrivilegedAction<Boolean> {
        private final File file;

        public FileExistencePrivilegedAction (File file) {
            this.file = file;
        }

        @Override
        public Boolean run() {
            return (file.exists() && !file.isDirectory());
        }
    }

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

    public static boolean fileExists (File file) {
        return AccessController.doPrivileged(new FileExistencePrivilegedAction(file));
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
