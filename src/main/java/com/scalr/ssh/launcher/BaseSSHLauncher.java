package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.logging.Loggable;

abstract public class BaseSSHLauncher extends Loggable implements SSHLauncherInterface {
    final SSHConfiguration sshConfiguration;
    protected final FileSystemManager fsManager;

    public BaseSSHLauncher (SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        this.sshConfiguration = sshConfiguration;
        this.fsManager = fsManager;
    }

    public BaseSSHLauncher (SSHConfiguration sshConfiguration) {
        this(sshConfiguration, new FileSystemManager());
    }
}
