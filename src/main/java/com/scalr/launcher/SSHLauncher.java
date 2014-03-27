package com.scalr.launcher;

import com.scalr.ssh.SSHConfiguration;
import com.scalr.exception.EnvironmentSetupException;
import com.scalr.exception.InvalidEnvironmentException;

import java.io.IOException;

public interface SSHLauncher {
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException;
    public void tearDownEnvironment ();
    public String[] getSSHCommand() throws InvalidEnvironmentException;
}
