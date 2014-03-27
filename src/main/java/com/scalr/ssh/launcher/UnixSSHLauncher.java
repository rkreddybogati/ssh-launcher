package com.scalr.ssh.launcher;

import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.manager.UnixSSHManager;

import java.io.*;

public abstract class UnixSSHLauncher implements SSHLauncher {
    File commandFile;

    void createCommandFile(String sshCommandLine) throws EnvironmentSetupException {
        try {
            commandFile = FileSystemManager.getTemporaryFile("ssh-command", ".sh");
        } catch (IOException e) {
            throw new EnvironmentSetupException("Error creating command file.");
        }
        //commandFile.deleteOnExit(); // TODO: Find how we handle this

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(commandFile, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new EnvironmentSetupException("Error writing to command file.");
        } catch (UnsupportedEncodingException e) {
            throw new EnvironmentSetupException("Error writing to command file.");
        }

        writer.println("#!/bin/bash");
        writer.println(sshCommandLine);
        writer.close();

        if (!commandFile.setExecutable(true, true)) {
            throw new EnvironmentSetupException("Error setting command file executable.");
        }
    }

    @Override
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws EnvironmentSetupException {
        UnixSSHManager sshManager = new UnixSSHManager(sshConfiguration);
        sshManager.setUpSSHEnvironment();
        String sshCommandLine = sshManager.getSSHCommandLine();
        createCommandFile(sshCommandLine);
    }

    @Override
    public void tearDownEnvironment() {
    }

    abstract protected String[] getSSHCommandFromPath (String path);

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
}
