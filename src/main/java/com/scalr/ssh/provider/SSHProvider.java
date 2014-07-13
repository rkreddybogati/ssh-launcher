package com.scalr.ssh.provider;

import com.scalr.ssh.exception.LauncherException;

public interface SSHProvider {
    public String[] getSSHCommand() throws LauncherException;
}
