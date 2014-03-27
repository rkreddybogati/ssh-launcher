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

    protected File getTemporaryFile (final String prefix, final String suffix) throws IOException {
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

    protected String getSSHCommandLine(SSHConfiguration sshConfiguration) {
        String[] destinationBits = {sshConfiguration.getUsername(), "@", sshConfiguration.getHost()};
        String   destination = StringUtils.join(destinationBits, "");

        String[] sshCommandLineBits = {"ssh", destination};
        return StringUtils.join(sshCommandLineBits, " ");
    }

    @Override
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException {

        commandFile = getTemporaryFile("ssh-command", ".sh");
        //commandFile.deleteOnExit(); // TODO: Find how we handle this

        String   sshCommandLine = getSSHCommandLine(sshConfiguration);

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
