package com.scalr.ssh.filesystem;

import com.scalr.ssh.logging.Loggable;

import java.io.*;
import java.security.PrivilegedAction;
import java.util.logging.Level;

public class WritePrivateKeyPrivilegedAction extends Loggable implements PrivilegedAction<Boolean> {
    File keyFile;
    String privateKey;

    public WritePrivateKeyPrivilegedAction(File keyFile, String privateKey) {
        this.keyFile = keyFile;
        this.privateKey = privateKey;
    }

    @Override
    public Boolean run() {
        // Used in logging.
        String keyPath = keyFile.getPath();

        if (!keyFile.getParentFile().exists() && !keyFile.getParentFile().mkdirs()) {
            getLogger().severe(String.format("Failed to create directory tree for SSH File '%s'",
                    keyFile.getPath()));
            return false;
        }

        try {
            getLogger().finer(String.format("Creating new SSH file: '%s'", keyPath));
            if (!keyFile.createNewFile()) {
                getLogger().severe(String.format("Failed to create SSH key file: %s", keyPath));
                return false;
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, String.format("Error creating SSH File '%s'", keyPath), e);
            return false;
        }

        if (!keyFile.setWritable(false, false) || !keyFile.setWritable(true, true)) {
            getLogger().warning(String.format("Failed to make SSH File '%s' writable", keyPath));
        }

        if (!keyFile.setReadable(false, false) || !keyFile.setReadable(true, true)) {
            getLogger().warning(String.format("Failed to make SSH File '%s' writable", keyPath));
        }

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(keyFile), "UTF-8");
            BufferedWriter output = new BufferedWriter(writer);
            output.write(privateKey);
            output.close();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error writing private key to SSH File", e);
            return false;
        }

        return true;
    }
}

