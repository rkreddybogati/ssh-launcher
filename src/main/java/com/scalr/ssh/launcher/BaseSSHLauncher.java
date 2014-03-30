package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.logging.Loggable;

abstract public class BaseSSHLauncher extends Loggable implements SSHLauncherInterface {
    final SSHConfiguration sshConfiguration;

    public BaseSSHLauncher (SSHConfiguration sshConfiguration) {
        this.sshConfiguration = sshConfiguration;
    }
}
