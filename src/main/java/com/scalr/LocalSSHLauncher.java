package com.scalr;

import java.io.IOException;

public interface LocalSSHLauncher {
    public void setUpEnvironment() throws IOException, EnvironmentSetupException;
    public void tearDownEnvironment ();
    public String[] getSSHCommand() throws InvalidEnvironmentException;
}
