package com.scalr.ssh.filesystem;

import java.io.File;
import java.security.PrivilegedAction;

public class FileExistencePrivilegedAction implements PrivilegedAction<Boolean> {
    private final File file;

    public FileExistencePrivilegedAction (File file) {
        this.file = file;
    }

    @Override
    public Boolean run() {
        return (file.exists() && !file.isDirectory());
    }
}

