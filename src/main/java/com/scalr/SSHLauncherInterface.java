package com.scalr;

import java.io.IOException;

public interface SSHLauncherInterface {
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException;
    public void tearDownEnvironment ();
    public String[] getSSHCommand() throws InvalidEnvironmentException;
}
