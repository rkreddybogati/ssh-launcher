package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;

import java.io.IOException;

public interface SSHLauncherInterface {
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException;
    public void tearDownEnvironment ();
    public String[] getSSHCommand() throws InvalidEnvironmentException;
}
