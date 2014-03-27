package com.scalr.launcher;

import com.scalr.SSHConfiguration;
import com.scalr.exception.EnvironmentSetupException;
import com.scalr.exception.InvalidEnvironmentException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class UnixLauncher implements SSHLauncherInterface {
    File commandFile;

    @Override
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException {

        commandFile = AccessController.doPrivileged(
                new PrivilegedAction<File>() {
                    @Override
                    public File run() {
                        try {
                            return File.createTempFile("ssh-command", ".sh");
                        } catch (IOException e) {
                            return null;
                        }
                    }
                }
        );

        if (commandFile == null) {
            throw new IOException();
        }

        //commandFile.deleteOnExit(); // TODO: Find how we handle this

        String[] destinationBits = {sshConfiguration.getUsername(), "@", sshConfiguration.getHost()};
        String   destination = StringUtils.join(destinationBits, "");

        String[] sshCommandLineBits = {"ssh", destination};
        String   sshCommandLine = StringUtils.join(sshCommandLineBits, " ");

        PrintWriter writer = new PrintWriter(commandFile, "UTF-8");
        writer.println("#!/bin/bash");
        writer.println(sshCommandLine);  //TODO: Implement for real
        writer.close();

        if (!commandFile.setExecutable(true, true)) {
            throw new EnvironmentSetupException();
        }
    }

    @Override
    public void tearDownEnvironment() {
    }

    @Override
    public String[] getSSHCommand() throws InvalidEnvironmentException {
        if (commandFile == null) {
            throw new InvalidEnvironmentException();
        }

        String canonicalPath;

        try {
            canonicalPath = commandFile.getCanonicalPath();
        } catch (IOException e) {
            throw new InvalidEnvironmentException();
        }

        return getSSHCommandFromPath(canonicalPath);
    }

    abstract protected String[] getSSHCommandFromPath (String path);
}
