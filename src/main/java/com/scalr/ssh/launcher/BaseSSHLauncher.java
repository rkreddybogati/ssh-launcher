package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;

abstract public class BaseSSHLauncher implements SSHLauncherInterface {
    final SSHConfiguration sshConfiguration;

    public BaseSSHLauncher (SSHConfiguration sshConfiguration) {
        this.sshConfiguration = sshConfiguration;
    }
}
