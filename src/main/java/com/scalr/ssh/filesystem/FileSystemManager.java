package com.scalr.ssh.filesystem;

import com.scalr.ssh.logging.Loggable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class FileSystemManager extends Loggable {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

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
        return AccessController.doPrivileged(new FileExistsPrivilegedAction(file));
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

    public void writeFile (File file, String contents) throws IOException {
        // TODO - AccessController
        FileOutputStream out = new FileOutputStream(file);

        try {
            Writer writer = new OutputStreamWriter(out, UTF_8);
            writer.write(contents);
            writer.flush();
        } finally {
            out.close();
        }
    }

    public String readFile (File file) throws IOException {
        // TODO - AccessController
        FileInputStream in = new FileInputStream(file);

        try {
            Reader reader = new InputStreamReader(in, UTF_8);
            StringWriter writer = new StringWriter();
            IOUtils.copy(reader, writer);
            return writer.toString();
        } finally {
            in.close();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void chmod600 (File file) {
        file.setReadable(false, false);
        file.setReadable(true, true);
        file.setWritable(false, false);
        file.setWritable(true, true);
        file.setExecutable(false, false);
    }
}
