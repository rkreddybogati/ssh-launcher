package com.scalr.launcher;

import com.scalr.SSHConfiguration;
import com.scalr.exception.EnvironmentSetupException;
import com.scalr.exception.InvalidEnvironmentException;

import java.io.IOException;

public interface SSHLauncherInterface {
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException;
    public void tearDownEnvironment ();
    public String[] getSSHCommand() throws InvalidEnvironmentException;
}
