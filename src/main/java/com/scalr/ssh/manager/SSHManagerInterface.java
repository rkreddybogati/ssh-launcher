package com.scalr.ssh.manager;

import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;

public interface SSHManagerInterface {
    public void setUpSSHEnvironment() throws EnvironmentSetupException;
    public String[] getSSHCommandLineBits() throws InvalidEnvironmentException;
}
