package com.scalr.ssh.launcher;

import com.scalr.ssh.exception.LauncherException;

public interface SSHLauncherInterface {
    public String[] getSSHCommand() throws LauncherException;
}
