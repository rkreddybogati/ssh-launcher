package com.scalr.launcher;

import com.scalr.exception.EnvironmentSetupException;
import com.scalr.exception.InvalidEnvironmentException;
import com.scalr.fs.FileSystemManager;
import com.scalr.ssh.SSHConfiguration;
import com.scalr.ssh.UnixSSHManager;

import java.io.*;

public abstract class UnixSSHLauncher implements SSHLauncher {
    File commandFile;

    void createCommandFile(String sshCommandLine) throws EnvironmentSetupException {
        try {
            commandFile = FileSystemManager.getTemporaryFile("ssh-command", ".sh");
        } catch (IOException e) {
            e.printStackTrace();
            throw new EnvironmentSetupException();
        }
        //commandFile.deleteOnExit(); // TODO: Find how we handle this

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(commandFile, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new EnvironmentSetupException();
        } catch (UnsupportedEncodingException e) {
            throw new EnvironmentSetupException();
        }

        writer.println("#!/bin/bash");
        writer.println(sshCommandLine);
        writer.close();

        if (!commandFile.setExecutable(true, true)) {
            throw new EnvironmentSetupException();
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
