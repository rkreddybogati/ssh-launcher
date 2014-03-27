package com.scalr.ssh;

import com.scalr.exception.EnvironmentSetupException;

public interface SSHManager {
    public void setUpSSHEnvironment() throws EnvironmentSetupException;
    public String getSSHCommandLine ();
}
