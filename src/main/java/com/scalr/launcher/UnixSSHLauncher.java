package com.scalr.launcher;

import com.scalr.exception.EnvironmentSetupException;
import com.scalr.exception.InvalidEnvironmentException;
import com.scalr.ssh.SSHConfiguration;
import com.scalr.ssh.UnixSSHManager;

import java.io.File;
import java.io.IOException;

public abstract class UnixSSHLauncher implements SSHLauncher {
    File commandFile;

    abstract void createCommandFile (String sshCommandLine) throws EnvironmentSetupException;

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
