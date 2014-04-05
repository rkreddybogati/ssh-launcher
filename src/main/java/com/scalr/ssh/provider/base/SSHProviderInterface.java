package com.scalr.ssh.provider.base;

import com.scalr.ssh.exception.LauncherException;

public interface SSHProviderInterface {
    public String[] getSSHCommand() throws LauncherException;
}
