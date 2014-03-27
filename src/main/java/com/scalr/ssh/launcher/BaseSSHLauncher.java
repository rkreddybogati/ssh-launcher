package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.manager.SSHManagerInterface;

import java.io.File;
import java.io.IOException;

abstract public class BaseSSHLauncher implements SSHLauncherInterface {
    File commandFile;

    @Override
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws EnvironmentSetupException {
        SSHManagerInterface sshManager = getSSHManager(sshConfiguration);
        sshManager.setUpSSHEnvironment();
        String sshCommandLine = sshManager.getSSHCommandLine();
        createCommandFile(sshCommandLine);
    }

    abstract protected void createCommandFile(String sshCommandLine) throws EnvironmentSetupException;
    abstract protected SSHManagerInterface getSSHManager(SSHConfiguration sshConfiguration);

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
