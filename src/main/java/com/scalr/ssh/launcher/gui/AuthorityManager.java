package com.scalr.ssh.launcher.gui;

import com.scalr.ssh.filesystem.FileSystemManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileLock;

public class AuthorityManager {
    private Integer port;
    private String key;
    private FileSystemManager fs;

    private FileInputStream in;
    private FileLock lock;

    public Boolean lockAuthority () {
        if (lock != null) {
            // Already locked..
            return Boolean.FALSE;
        }

        try {
            // TODO - Change
            in = new FileInputStream("/tmp/ssh-authority");
        } catch (FileNotFoundException e) {
            // Weirdly enough, this is thrown for permission errors as well.
            return Boolean.FALSE;
        }

        try {
            lock = in.getChannel().lock();
        } catch (IOException e) {
            // TODO - Log!
            try {
                in.close();
            } catch (IOException e1) {
                //TODO - ...
            }
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }

    public Boolean unlockAuthority () {
        if (lock == null) {
            // TODO - Use exceptions
            return Boolean.FALSE;
        }

        try {
            lock.release();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock = null;
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in = null;
        }

        return Boolean.TRUE;
    }
}
