package com.scalr.ssh.manager;

import com.scalr.ssh.exception.EnvironmentSetupException;

public interface SSHManagerInterface {
    public void setUpSSHEnvironment() throws EnvironmentSetupException;
    public String getSSHCommandLine ();
}
