package com.scalr.ssh.provider;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.logging.Loggable;

abstract public class BaseSSHProvider extends Loggable implements SSHProviderInterface {
    final SSHConfiguration sshConfiguration;
    protected final FileSystemManager fsManager;

    public BaseSSHProvider(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        this.sshConfiguration = sshConfiguration;
        this.fsManager = fsManager;
    }

    public BaseSSHProvider(SSHConfiguration sshConfiguration) {
        this(sshConfiguration, new FileSystemManager());
    }
}
