package com.scalr.ssh.manager;

import com.scalr.ssh.exception.LauncherException;

public interface SSHManagerInterface {
    public void setUpSSHEnvironment() throws LauncherException;
    public String[] getSSHCommandLineBits() throws LauncherException;
}
