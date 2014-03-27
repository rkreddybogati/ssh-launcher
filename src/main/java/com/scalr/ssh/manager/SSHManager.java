package com.scalr.ssh.manager;

import com.scalr.ssh.exception.EnvironmentSetupException;

public interface SSHManager {
    public void setUpSSHEnvironment() throws EnvironmentSetupException;
    public String getSSHCommandLine ();
}
