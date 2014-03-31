package com.scalr.ssh.fs;

import com.scalr.ssh.logging.Loggable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class FileSystemManager extends Loggable {

    private class FileExistencePrivilegedAction implements PrivilegedAction<Boolean> {
        private final File file;

        public FileExistencePrivilegedAction (File file) {
            this.file = file;
        }

        @Override
        public Boolean run() {
            return (file.exists() && !file.isDirectory());
        }
    }

    public File getTemporaryFile (final String prefix, final String suffix) throws IOException {
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

    public boolean fileExists (File file) {
        return AccessController.doPrivileged(new FileExistencePrivilegedAction(file));
    }

    public File findInPaths (File[] containingFiles, String needle) {
        File candidateFile;
        getLogger().fine(String.format("Searching for '%s' in '%s'", needle, StringUtils.join(containingFiles, ", ")));

        for (File containingFile : containingFiles) {
            candidateFile = new File(containingFile, needle);
            if (fileExists(candidateFile)) {
                getLogger().fine(String.format("File '%s' was found in '%s'", needle, containingFile));
                return candidateFile;
            } else {
                getLogger().finer(String.format("File '%s' was not found in '%s'", needle, containingFile));
            }
        }

        getLogger().warning(String.format("Unable to find file '%s'", needle));
        return null;
    }

    public File pathJoin (String... pathElements) {
        File ret = null;

        for (String pathElement : pathElements) {
            if (ret == null) {
                ret = new File(pathElement);
            } else {
                ret = new File(ret, pathElement);
            }
        }
        return ret;
    }

    public String getUserHome () {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("user.home");
            }
        });
    }
}
